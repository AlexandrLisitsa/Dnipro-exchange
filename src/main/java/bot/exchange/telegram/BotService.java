package bot.exchange.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
public class BotService extends TelegramLongPollingCommandBot {

    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;

    @Autowired
    private MessageProcessor messageProcessor;

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public void onUpdatesReceived(List<Update> updates) {
        updates.forEach(update -> messageProcessor.process(update));
    }

    public void processNonCommandUpdate(Update update) {
        log.error("Non command");
    }

    public void processInvalidCommandUpdate(Update update) {
        super.processInvalidCommandUpdate(update);
    }

    public boolean filter(Message message) {
        return super.filter(message);
    }

    public void sendMessage(String chatId, String text, InlineKeyboardMarkup keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setReplyMarkup(keyboardMarkup);
        message.setChatId(chatId);
        message.setText(text);

        sendMessage(message);
    }

    public void sendMessage(String chatId, String text){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        sendMessage(message);
    }

    public void editMessage(EditMessageReplyMarkup replyMarkup) {
        try {
            execute(replyMarkup);
        } catch (TelegramApiException e) {
            log.error("Error editing message", e);
        }
    }

    public void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }
}
