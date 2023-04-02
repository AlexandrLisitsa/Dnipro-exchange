package bot.exchange.statemachine.transitions.exchange;

import bot.exchange.service.rest.ExchangeHttpService;
import bot.exchange.statemachine.Event;
import bot.exchange.statemachine.Payload;
import bot.exchange.statemachine.State;
import bot.exchange.statemachine.transitions.Transition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@WithStateMachine
public class PreConfirmExchange extends Transition {

    @Autowired
    private ExchangeHttpService exchangeHttpService;

    @Override
    public void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception {
        transitions.withExternal()
                .source(State.CURRENCY_AMOUNT)
                .target(State.PRE_CONFIRM_EXCHANGE)
                .event(Event.PRE_CONFIRM_EXCHANGE)
                .action(this)
                .and()
                .withExternal()
                .source(State.PRE_CONFIRM_EXCHANGE)
                .target(State.MAIN_MENU)
                .event(Event.MAIN_MENU)
                .action(mainMenu);
    }

    @Override
    public State getState() {
        return State.PRE_CONFIRM_EXCHANGE;
    }

    @Override
    public void execute(StateContext<State, Event> context) {

        Payload payload = getPayload(context);
        String[] split = payload.getContext().split(",");
        int exchangerId = Integer.parseInt(split[0]);
        boolean isEnough = Boolean.parseBoolean(split[1]);

        context.getExtendedState().getVariables().put("exchangerId", exchangerId);
        context.getExtendedState().getVariables().put("isEnough", isEnough);

        ExchangeHttpService.CommitExchangeResponse exchangeResponse = (ExchangeHttpService.CommitExchangeResponse) context.getExtendedState().getVariables().get("exchange");

        String operationDetails = getOperationDetails(exchangeResponse);
        InlineKeyboardMarkup confirmButtons = getConfirmButtons();
        long chatId = getChatId(context);
        botService.sendMessage(String.valueOf(chatId), operationDetails, confirmButtons);

    }

    private InlineKeyboardMarkup getConfirmButtons() {
        InlineKeyboardButton approve = InlineKeyboardButton.builder()
                .text("Підтвердити")
                .callbackData(Event.CONFIRM_EXCHANGE + ";")
                .build();
        InlineKeyboardButton decline = InlineKeyboardButton.builder()
                .text("Відмінити")
                .callbackData(Event.MAIN_MENU + ";")
                .build();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = Arrays.asList(
                Collections.singletonList(approve),
                Collections.singletonList(decline)
        );
        markup.setKeyboard(buttons);

        return markup;
    }

    private String getOperationDetails(ExchangeHttpService.CommitExchangeResponse response) {
        ExchangeHttpService.Operation operation = response.getOperation();
        List<ExchangeHttpService.Exchanger> exchangers = response.getAvaliable_exchangers();
        StringBuilder confirmMessage = new StringBuilder();
        confirmMessage.append("Напрям обміну: ").append(operation.getDirection()).append("\n");
        confirmMessage.append("Ваш курс: ").append(operation.getRate()).append("\n");
        confirmMessage.append("Віддаєте: ").append(operation.getAmount()).append("\n");
        confirmMessage.append("Отримуєте: ").append(operation.getReceive()).append("\n\n");
        confirmMessage.append("Курс для вашої операції зафіксовано до ").append(response.getOperationTime());

        return confirmMessage.toString();
    }

}
