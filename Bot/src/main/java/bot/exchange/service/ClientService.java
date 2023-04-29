package bot.exchange.service;

import bot.exchange.db.entity.Client;
import bot.exchange.db.repository.UserRepo;
import bot.exchange.service.rest.ClientHttpService;
import bot.exchange.service.rest.MenorahHttpService;
import bot.exchange.statemachine.StateMachineService;
import bot.exchange.telegram.BotService;
import bot.exchange.telegram.Icons;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private MenorahHttpService menorahHttpService;

    @Autowired
    private StateMachineService stateMachineService;

    @Autowired
    private UserRepo userRepo;
    private DateTimeFormatter dateTemplate = DateTimeFormatter.ofPattern("dd.MM.yyyy на HH.mm");

    public boolean isUserIdPresent(Update update) {
        return update.getMessage().getChat().getUserName() != null;
    }

    public void sendIdentityErrorMessage(Update update) {
        String chatId = String.valueOf(update.getMessage().getChat().getId());
        botService.sendMessage(chatId,
                "Для продовження роботи с ботом необхідно встановити username.");
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
        clientCreationErrorMessage.setText("Помилка реєстрації. Зв'яжіться з технічною підтримкою.");

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
        authMessage.setText(getGreetingMessage());

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
        keyboardButton.setText("Увійти");
        keyboardButton.setRequestContact(true);
        keyboardFirstRow.add(keyboardButton);

        // add array to list
        keyboard.add(keyboardFirstRow);

        // add list to our keyboard
        replyKeyboardMarkup.setKeyboard(keyboard);

        botService.sendMessage(authMessage);
    }

    private String getGreetingMessage() {
        StringBuilder message = new StringBuilder();
        message.append("\uD83D\uDCB0\uD83D\uDCB0\uD83D\uDCB0 Обмін валют Дай Долар! \uD83D\uDCB0\uD83D\uDCB0\uD83D\uDCB0").append("\n\n");

        String date = LocalDateTime.now().format(dateTemplate);
        message.append("Раздрібний курс на ").append(date).append("\n\n");

        LinkedHashMap<String, MenorahHttpService.Rates> menorahRates = menorahHttpService.getMenorahRates();
        menorahRates.forEach((currency, rate) -> {
            String currencyUpperCase = currency.toUpperCase();
            message.append(Icons.CURRENCY_ICONS.get(currencyUpperCase)).append(currencyUpperCase)
                    .append(" ").append(rate.getBuy()).append("/").append(rate.getSell()).append("\n");
        });
        message.append("\n");

        message.append("\uD83D\uDCC8\uD83D\uDCC9 Курс залежить від суми обміну і вашої персональної знижки. Реєструйся у нашому і міняй валюту за найвигіднішим курсом у м. Дніпро!\n" +
                "Для отримання спеціальних знижок натиснить кнопку \n" +
                "⬇️Увійти⬇️");

        return message.toString();
    }

}
