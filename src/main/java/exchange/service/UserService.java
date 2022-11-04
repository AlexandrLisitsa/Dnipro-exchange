package exchange.service;

import exchange.telegram.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private BotService botService;

    @Autowired
    private void setBotService(@Lazy BotService botService) {
        this.botService = botService;
    }

    public boolean isUserIdPresent(Update update) {
        return update.getMessage().getChat().getUserName() != null;
    }

    public void sendIdentityErrorMessage(Update update) {
        String chatId = String.valueOf(update.getMessage().getChat().getId());
        botService.sendMessage(chatId,
                "Для продолжения работы с ботом необходимо установить username.");
    }

    public void handleUser(Update update) {

    }

    private boolean isUserExists(String phoneNumber){
        return true;
    }

    public void sendAuthorizationRequest(Update update) {
        SendMessage authMessage = new SendMessage();
        authMessage.setChatId(update.getMessage().getChatId());
        authMessage.setText("Ваш номер телефона");

        // create keyboard
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        authMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        // new list
        List<KeyboardRow> keyboard = new ArrayList<>();

        // first keyboard line
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText("Отправить номер телефона.");
        keyboardButton.setRequestContact(true);
        keyboardFirstRow.add(keyboardButton);

        // add array to list
        keyboard.add(keyboardFirstRow);

        // add list to our keyboard
        replyKeyboardMarkup.setKeyboard(keyboard);

        botService.sendMessage(authMessage);
    }
}
