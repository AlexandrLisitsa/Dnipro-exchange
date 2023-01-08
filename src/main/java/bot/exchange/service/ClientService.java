package bot.exchange.service;

import bot.exchange.db.entity.Client;
import bot.exchange.db.repository.UserRepo;
import bot.exchange.service.rest.ClientHttpService;
import bot.exchange.statemachine.StateMachineService;
import bot.exchange.telegram.BotService;
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

    @Autowired
    private UserRepo userRepo;

    public boolean isUserIdPresent(Update update) {
        return update.getMessage().getChat().getUserName() != null;
    }

    public void sendIdentityErrorMessage(Update update) {
        String chatId = String.valueOf(update.getMessage().getChat().getId());
        botService.sendMessage(chatId,
                "Для продолжения работы с ботом необходимо установить username.");
    }

    public void handleClient(Update update) {
        String phoneNumber = getPhoneNumber(update);
        log.info("Clients number: " + phoneNumber);
        String userName = update.getMessage().getChat().getUserName();
        Long chatId = update.getMessage().getChat().getId();
        if (isClientExists(phoneNumber)) {
            stateMachineService.initStateMachine(userName, chatId, phoneNumber);
            updateOrCreateClient(String.valueOf(chatId), userName, phoneNumber);
        } else {
            createClientAndStart(phoneNumber, userName, chatId);
        }
    }

    private String getPhoneNumber(Update update) {
        String phoneNumber = update.getMessage().getContact().getPhoneNumber();
        if (!phoneNumber.contains("+")) {
            phoneNumber = "+" + phoneNumber;
        }
        return phoneNumber;
    }

    private void updateOrCreateClient(String chatId, String userId, String phone) {
        Client client = userRepo.getClientByUserId(userId);
        if (client == null) {
            client = new Client();
            client.setUserId(userId);
        }
        client.setChatId(chatId);
        client.setPhone(phone);

        userRepo.save(client);
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
        authMessage.setText("Для продовження відправте номер телефону.");

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
        keyboardButton.setText("Відправити номер телефону.");
        keyboardButton.setRequestContact(true);
        keyboardFirstRow.add(keyboardButton);

        // add array to list
        keyboard.add(keyboardFirstRow);

        // add list to our keyboard
        replyKeyboardMarkup.setKeyboard(keyboard);

        botService.sendMessage(authMessage);
    }
}
