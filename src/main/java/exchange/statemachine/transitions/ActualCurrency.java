package exchange.statemachine.transitions;

import exchange.statemachine.Event;
import exchange.statemachine.Payload;
import exchange.statemachine.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;
import java.util.List;

@WithStateMachine
public class ActualCurrency extends Transition {

    @Autowired
    MainMenu mainMenuAction;

    @Override
    public void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception {
        transitions.withExternal()
                .source(State.MAIN_MENU)
                .target(State.RATES)
                .event(Event.RATES)
                .action(this)
                .and()
                .withExternal()
                .source(State.RATES)
                .event(Event.MAIN_MENU)
                .target(State.MAIN_MENU)
                .action(mainMenuAction);
    }

    @Override
    public State getState() {
        return State.RATES;
    }

    @Override
    public void execute(StateContext<State, Event> context) {
        Payload payload = (Payload) context.getExtendedState().getVariables().get("payload");

        InlineKeyboardButton back = new InlineKeyboardButton();
        back.setText("Назад");
        back.setCallbackData(Event.MAIN_MENU.toString());

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = Collections.singletonList(
                Collections.singletonList(back));
        inlineKeyboardMarkup.setKeyboard(buttons);

        // TODO: 12.10.2022 Replace with a rest call
        StringBuilder rateText = new StringBuilder("<<< Курсы валют >>>").append("\n");
        rateText.append("Доллар: 41/42").append("\n");
        rateText.append("Евро: 40/41");


        botService.sendMessage(payload.getChatId(), rateText.toString(), inlineKeyboardMarkup);
    }
}
