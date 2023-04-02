package bot.exchange.statemachine.transitions.exchange;

import bot.exchange.statemachine.Event;
import bot.exchange.statemachine.Payload;
import bot.exchange.statemachine.State;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@WithStateMachine
public class ToCurrency extends CurrencyDirections {
    @Override
    public void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception {
        transitions.withExternal()
                .source(State.FROM_CURRENCY)
                .event(Event.TO_CURRENCY)
                .target(State.TO_CURRENCY)
                .action(this)
                .and()
                .withExternal()
                .source(State.TO_CURRENCY)
                .target(State.MAIN_MENU)
                .event(Event.MAIN_MENU)
                .action(mainMenuAction);
    }

    @Override
    public State getState() {
        return State.TO_CURRENCY;
    }

    @Override
    public void execute(StateContext<State, Event> context) {
        Payload payload = (Payload) context.getExtendedState().getVariables().get("payload");

        setFrom(context);

        List<List<InlineKeyboardButton>> keyboardMarkup = getKeyboardMarkupExcludeCurrency(
                State.CURRENCY_AMOUNT,
                Direction.TO,
                getCurrenciesToExclude(getFrom(context)));
        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().keyboard(keyboardMarkup).build();
        botService.sendMessage(payload.getChatId(), "Оберіть напрямок обміну (в яку валюту)", markup);
    }

    private List<String> getCurrenciesToExclude(String fromCurrency) {
        List<String> currenciesToExclude;
        if (!fromCurrency.equalsIgnoreCase("UAH")) {
            currenciesToExclude = banknotes.stream().filter(banknote -> {
                return !banknote.equalsIgnoreCase("UAH");
            }).collect(Collectors.toList());
        } else {
            currenciesToExclude = Collections.singletonList("UAH");
        }
        return currenciesToExclude;
    }

}
