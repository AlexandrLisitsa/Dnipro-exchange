package exchange.service;

import exchange.service.rest.ClientHttpService;
import exchange.statemachine.StateMachineService;
import exchange.telegram.BotService;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;

@Slf4j
@Service
public class ClientService {

    private BotService botService;

    @Autowired
    private void setBotService(@Lazy BotService botService) {
        this.botService = botService;
    }

    @Autowired
    private ClientHttpService clientHttpService;

    @Autowired
    private StateMachineService stateMachineService;

    public boolean isUserIdPresent(Update update) {
        return update.getMessage().getChat().getUserName() != null;
    }

    public void sendIdentityErrorMessage(Update update) {
        String chatId = String.valueOf(update.getMessage().getChat().getId());
        botService.sendMessage(chatId,
                "Для продолжения работы с ботом необходимо установить username.");
    }

    public void handleClient(Update update) {
        String phoneNumber = "+" + update.getMessage().getContact().getPhoneNumber();
        String userName = update.getMessage().getChat().getUserName();
        Long chatId = update.getMessage().getChat().getId();
        if (isClientExists(phoneNumber)) {
            stateMachineService.initStateMachine(userName, chatId, phoneNumber);
        } else {
            createClientAndStart(phoneNumber, userName, chatId);
        }
    }

    private void createClientAndStart(String phoneNumber, String userName, Long chatId) {
        boolean isCreated = clientHttpService.createClient(phoneNumber);
        if (isCreated) {
            stateMachineService.initStateMachine(userName, chatId, phoneNumber);
        } else {
            sendClientCreationErrorMessage(chatId);
        }
    }

    private void sendClientCreationErrorMessage(Long chatId) {
        SendMessage clientCreationErrorMessage = new SendMessage();
        clientCreationErrorMessage.setChatId(chatId);
        clientCreationErrorMessage.setText("Ошибка регистарции. Свяжитесь с технической поддержкой.");

        botService.sendMessage(clientCreationErrorMessage);
    }

    private boolean isClientExists(String phoneNumber) {
        Optional<ClientHttpService.Client> clientInfo = clientHttpService.getClientInfo(phoneNumber);
        if (clientInfo.isPresent()) {
            return clientInfo.get().getPhone().equalsIgnoreCase(phoneNumber);
        }
        log.error("Error getting client info by number:" + phoneNumber);
        return false;
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
