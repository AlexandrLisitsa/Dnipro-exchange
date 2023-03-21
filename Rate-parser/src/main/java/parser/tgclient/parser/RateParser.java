package parser.tgclient.parser;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class RateParser {

    private final Pattern ratePattern = Pattern.compile("\\S+ (\\d+.\\d+ / \\d+.\\d+)", Pattern.CASE_INSENSITIVE);
    @Autowired
    private Client client;
    private long cachedMenorahId = -1;

    public MenorahRates getMenorahRates() {
        MenorahRates rates = null;
        try {
            TdApi.Chats chats = getChats();
            TdApi.Chat menorahChat = getMenorahChat(chats);
            rates = getRates(menorahChat);
            System.out.println(rates);

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error getting chats", e);
        }
        return rates;
    }

    private TdApi.Chats getChats() throws ExecutionException, InterruptedException {
        return (TdApi.Chats) client.send(new TdApi.GetChats(null, 100)).get();
    }

    private TdApi.Chat getMenorahChat(TdApi.Chats chats) throws ExecutionException, InterruptedException {
        TdApi.Chat menorahChat = null;
        if (cachedMenorahId != -1) {
            menorahChat = (TdApi.Chat) client.send(new TdApi.GetChat(cachedMenorahId)).get();
        }
        if (menorahChat == null || !menorahChat.title.contains("МЕНОРА")) {
            for (long chatId : chats.chatIds) {
                TdApi.Chat chat = (TdApi.Chat) client.send(new TdApi.GetChat(chatId)).get();
                if (chat.title.contains("МЕНОРА")) {
                    menorahChat = chat;
                    cachedMenorahId = chatId;
                    break;
                }
            }
        }
        return menorahChat;
    }

    private MenorahRates getRates(TdApi.Chat chat) throws ExecutionException, InterruptedException {
        Optional<TdApi.Message> lastRateMessage = getLastRateMessage(chat);
        TdApi.Message lastRate = lastRateMessage.orElseThrow(() -> new RuntimeException("Message is not found"));
        String text = ((TdApi.MessageText) lastRate.content).text.text;

        RateLanguage messageLanguage = getMessageLanguage(text);

        MenorahRates menorahRates = null;
        if (messageLanguage == RateLanguage.UA) {
            menorahRates = parseUaMessage(text);
        } else {
            menorahRates = parseRuMessage(text);
        }
        return menorahRates;
    }

    MenorahRates parseUaMessage(String text) {
        String[] lines = text.split("\n");

        MenorahRates menorahRates = new MenorahRates();
        String date = parseDate(lines, RateLanguage.UA);
        LocalDateTime now = LocalDateTime.now();
        String format = DateTimeFormatter.ofPattern("HH:mm").format(now);
        date = date + " " + format;
        menorahRates.setDate(date);

        List<Currency> currencies = parseCurrency(lines);
        menorahRates.setCurrencies(currencies);

        return menorahRates;
    }

    MenorahRates parseRuMessage(String text) {
        String[] lines = text.split("\n");

        MenorahRates menorahRates = new MenorahRates();
        String date = parseDate(lines, RateLanguage.RUS);
        LocalDate now = LocalDate.now();
        String format = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(now);
        date = format + " " + date;
        menorahRates.setDate(date);

        List<Currency> currencies = parseCurrency(lines);
        menorahRates.setCurrencies(currencies);

        return menorahRates;
    }

    private List<Currency> parseCurrency(String[] lines) {
        List<Currency> currencies = new ArrayList<>();
        for (String line : lines) {
            Matcher matcher = ratePattern.matcher(line.trim());
            if (matcher.matches()) {
                String[] rates = matcher.group(1).split("/");
                double buy = Double.parseDouble(rates[0].trim());
                double sell = Double.parseDouble(rates[1].trim());

                Currency currency = new Currency();
                currency.setBuy(buy);
                currency.setSell(sell);

                String name = line.trim().split(" ")[0].trim();
                currency.setName(name);

                currencies.add(currency);
            }
        }

        return currencies;
    }

    private String parseDate(String[] lines, RateLanguage rateLanguage) {
        String date = null;
        for (String line : lines) {
            boolean contains = line.contains(rateLanguage.pattern);
            if (contains) {
                String[] words = line.split(" ");
                date = words[words.length - 1];
                break;
            }
        }

        return date;
    }

    private Optional<TdApi.Message> getLastRateMessage(TdApi.Chat chat) throws ExecutionException, InterruptedException {
        TdApi.Messages lastMessage = (TdApi.Messages) client.send(new TdApi.GetChatHistory(chat.id, 0, 0, 10, false)).get();
        long lastMessageId = lastMessage.messages[0].id;
        TdApi.Messages top10 = (TdApi.Messages) client.send(new TdApi.GetChatHistory(chat.id, lastMessageId, 0, 10, false)).get();

        List<TdApi.Message> messages = new ArrayList<>();
        messages.addAll(Arrays.asList(lastMessage.messages));
        messages.addAll(Arrays.asList(top10.messages));

        return messages.stream().filter(message -> {
            boolean isText = message.content instanceof TdApi.MessageText;
            RateLanguage rateLanguage = RateLanguage.NONE;
            if (isText) {
                String text = ((TdApi.MessageText) message.content).text.text;
                rateLanguage = getMessageLanguage(text);
            }

            return rateLanguage != RateLanguage.NONE;
        }).findFirst();
    }

    private RateLanguage getMessageLanguage(String message) {
        if (message.contains("Актуальный")) {
            return RateLanguage.RUS;
        } else if (message.contains("Актуальний")) {
            return RateLanguage.UA;
        }
        return RateLanguage.NONE;
    }

    private enum RateLanguage {
        UA("Актуальний"),
        RUS("Актуальный"),
        NONE("");

        private final String pattern;

        RateLanguage(String pattern) {
            this.pattern = pattern;
        }

        public String getPattern() {
            return pattern;
        }
    }

    @Data
    public static class MenorahRates {
        String date;
        List<Currency> currencies;
    }

    @Data
    public static class Currency {
        private String name;
        private double buy;
        private double sell;
    }

}
