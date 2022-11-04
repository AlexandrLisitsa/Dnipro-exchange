package exchange.telegram;

import exchange.service.UserService;
import exchange.statemachine.Event;
import exchange.statemachine.Payload;
import exchange.statemachine.StateMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class MessageProcessor {

    @Autowired
    private StateMachineService stateMachineService;
    @Autowired
    private UserService userService;

    public void process(Update update) {
        if (update.hasCallbackQuery()) {
            Payload payload = processCallback(update);
            stateMachineService.changeState(payload);
        } else if (update.getMessage().getContact() != null) {
            userService.handleUser(update);
        } else if (update.getMessage() != null && update.getMessage().getText().equalsIgnoreCase("/start")) {
            handleStartCommand(update);
        } else {
            Payload payload = processMessage(update);
            stateMachineService.changeState(payload);
        }
    }

    private void handleStartCommand(Update update) {
        if (userService.isUserIdPresent(update)) {
            userService.sendAuthorizationRequest(update);
        } else {
            userService.sendIdentityErrorMessage(update);
        }
    }

    private Payload processMessage(Update update) {
        Payload payload = new Payload();

        String chatId = String.valueOf(update.getMessage().getChat().getId());
        payload.setChatId(chatId);

        String userName = update.getMessage().getChat().getUserName();
        payload.setUserId(userName);

        String text = update.getMessage().getText();
        payload.setContext(text);

        Integer messageId = update.getMessage().getMessageId();
        payload.setMessageId(messageId);

        return payload;
    }

    private Payload processCallback(Update update) {
        Payload payload = new Payload();
        payload.setCallback(true);

        String userName = update.getCallbackQuery().getMessage().getChat().getUserName();
        payload.setUserId(userName);

        Long chatId = update.getCallbackQuery().getMessage().getChat().getId();
        payload.setChatId(String.valueOf(chatId));

        String[] split = update.getCallbackQuery().getData().split(";");

        if (StringUtils.hasText(split[0])) {
            Event event = Event.valueOf(split[0]);
            payload.setEvent(event);
        }

        if (split.length > 1) {
            payload.setContext(split[1]);
        }

        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        payload.setMessageId(messageId);

        return payload;
    }

}
