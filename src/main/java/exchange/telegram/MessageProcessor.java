package exchange.telegram;

import exchange.statemachine.Event;
import exchange.statemachine.Payload;
import exchange.statemachine.StateMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class MessageProcessor {

    @Autowired
    private StateMachineService stateMachineService;

    public void process(Update update) {
        if (update.hasCallbackQuery()) {
            Payload payload = processCallback(update);
            stateMachineService.changeState(payload);
        } else if (update.getMessage() != null && update.getMessage().getText().equalsIgnoreCase("/start")) {
            String userName = update.getMessage().getChat().getUserName();
            Long id = update.getMessage().getChat().getId();

            stateMachineService.initStateMachine(userName, id);
        }
    }

    private Payload processCallback(Update update) {
        Payload payload = new Payload();

        String userName = update.getCallbackQuery().getMessage().getChat().getUserName();
        payload.setUserId(userName);

        Long chatId = update.getCallbackQuery().getMessage().getChat().getId();
        payload.setChatId(chatId);

        String[] split = update.getCallbackQuery().getData().split(";");
        Event event = Event.valueOf(split[0]);
        payload.setEvent(event);

        if (split.length > 1) {
            payload.setContext(split[1]);
        }

        return payload;
    }

}
