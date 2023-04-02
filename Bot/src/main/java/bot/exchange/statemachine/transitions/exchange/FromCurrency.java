package bot.exchange.statemachine.transitions.exchange;

import bot.exchange.statemachine.Event;
import bot.exchange.statemachine.Payload;
import bot.exchange.statemachine.State;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@WithStateMachine
public class FromCurrency extends CurrencyDirections {

    @Override
    public void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception {
        transitions.withExternal()
                .source(State.MAIN_MENU)
                .action(this)
                .event(Event.FROM_CURRENCY)
                .target(State.FROM_CURRENCY)
                .and()
                .withExternal()
                .source(State.FROM_CURRENCY)
                .target(State.MAIN_MENU)
                .event(Event.MAIN_MENU)
                .action(mainMenuAction);
    }

    @Override
    public State getState() {
        return State.FROM_CURRENCY;
    }

    @Override
    public void execute(StateContext<State, Event> context) {
        Payload payload = (Payload) context.getExtendedState().getVariables().get("payload");

        List<List<InlineKeyboardButton>> keyboardMarkup = getKeyboardMarkup(State.TO_CURRENCY, Direction.FROM);
        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().keyboard(keyboardMarkup).build();
        botService.sendMessage(payload.getChatId(), "Оберіть напрямок обміну (з якої валюти)", markup);
    }

}
