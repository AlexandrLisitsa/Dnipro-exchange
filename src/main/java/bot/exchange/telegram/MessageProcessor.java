package bot.exchange.telegram;

import bot.commands.Command;
import bot.commands.CommandHandler;
import bot.commands.CommandPayload;
import bot.exchange.service.ClientService;
import bot.exchange.statemachine.Event;
import bot.exchange.statemachine.Payload;
import bot.exchange.statemachine.StateMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class MessageProcessor {

    @Autowired
    private StateMachineService stateMachineService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private CommandHandler commandHandler;

    public void process(Update update) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith("/")) {
            CommandPayload commandPayload = getCommandPayload(update);
            commandHandler.handleCommand(commandPayload);
        } else if (update.hasCallbackQuery()) {
            Payload payload = processCallback(update);
            stateMachineService.changeState(payload);
        } else if (update.getMessage().getContact() != null) {
            clientService.handleClient(update);
        } else if (update.getMessage() != null && update.getMessage().getText().equalsIgnoreCase("/start")) {
            handleStartCommand(update);
        } else {
            Payload payload = getPayload(update);
            stateMachineService.changeState(payload);
        }
    }

    private void handleStartCommand(Update update) {
        if (clientService.isUserIdPresent(update)) {
            clientService.sendAuthorizationRequest(update);
        } else {
            clientService.sendIdentityErrorMessage(update);
        }
    }

    private CommandPayload getCommandPayload(Update update) {
        CommandPayload commandPayload = new CommandPayload();

        String chatId = String.valueOf(update.getCallbackQuery().getMessage().getChat().getId());
        commandPayload.setChatId(chatId);

        String data = update.getCallbackQuery().getData();
        String[] split = data.split(";");

        String command = split[0].replace("/", "");
        commandPayload.setCommand(Command.valueOf(command));

        commandPayload.setData(split[1]);

        return commandPayload;
    }

    private Payload getPayload(Update update) {
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
