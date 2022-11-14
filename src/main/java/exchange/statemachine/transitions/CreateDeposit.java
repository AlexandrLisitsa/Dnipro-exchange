package exchange.statemachine.transitions;

import exchange.statemachine.Event;
import exchange.statemachine.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CreateDeposit extends Transition {

    @Autowired
    private MainMenu mainMenu;

    private List<String> banknotes = Arrays.asList("USD", "UAH", "EUR", "PLN");

    @Override
    public void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception {
        transitions.withExternal()
                .source(State.MAIN_MENU)
                .target(State.CREATE_DEPOSIT)
                .event(Event.CREATE_DEPOSIT)
                .action(this)
                .and()
                .withExternal()
                .source(State.CREATE_DEPOSIT)
                .target(State.MAIN_MENU)
                .event(Event.MAIN_MENU)
                .action(mainMenu);
    }

    @Override
    public State getState() {
        return State.CREATE_DEPOSIT;
    }

    @Override
    public void execute(StateContext<State, Event> context) {

    }

    private List<List<InlineKeyboardButton>> getCurrencyButtons() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        banknotes.forEach(banknote -> {
            List<InlineKeyboardButton> buttonRow = Collections.singletonList(
                    InlineKeyboardButton.builder()
                            .text(banknote)
                            .callbackData(";from " + banknote)
                            .build()
            );
            buttons.add(buttonRow);
        });
        buttons.add(Arrays.asList(InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData(Event.MAIN_MENU.toString())
                .build()));
        return buttons;
    }

}
