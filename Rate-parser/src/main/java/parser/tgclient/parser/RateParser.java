package parser.tgclient.parser;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class RateParser {

    private final Pattern ratePattern = Pattern.compile("\\S+ (\\d+.\\d+ / \\d+.\\d+)", Pattern.CASE_INSENSITIVE);

    public MenorahRates getRates(String messageContext) {

        RateLanguage messageLanguage = getMessageLanguage(messageContext);

        MenorahRates menorahRates = null;
        if (messageLanguage == RateLanguage.UA) {
            menorahRates = parseUaMessage(messageContext);
        } else {
            menorahRates = parseRuMessage(messageContext);
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
