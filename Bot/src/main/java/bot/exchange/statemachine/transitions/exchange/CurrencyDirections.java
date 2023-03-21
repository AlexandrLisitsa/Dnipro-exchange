package bot.exchange.statemachine.transitions.exchange;

import bot.exchange.statemachine.Event;
import bot.exchange.statemachine.Payload;
import bot.exchange.statemachine.State;
import bot.exchange.statemachine.transitions.MainMenu;
import bot.exchange.statemachine.transitions.Transition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CurrencyDirections extends Transition {

    private final String FROM_KEY = "currency_from";
    private final String TO_KEY = "currency_to";
    @Autowired
    protected MainMenu mainMenuAction;
    protected List<String> banknotes = Arrays.asList("USD", "UAH", "EUR", "PLN");

    @Override
    public void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception {
    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public void execute(StateContext<State, Event> context) {
    }

    protected String getDirection(StateContext<State, Event> context) {
        return getFrom(context) + "->" + getTo(context);
    }

    protected void setFrom(StateContext<State, Event> context) {
        Payload payload = getPayload(context);
        context.getExtendedState().getVariables().put(FROM_KEY, extractCurrencyFromMessage(payload.getContext()));
    }

    protected String getFrom(StateContext<State, Event> context) {
        return (String) context.getExtendedState().getVariables().get(FROM_KEY);
    }

    protected void setTo(StateContext<State, Event> context) {
        Payload payload = getPayload(context);
        context.getExtendedState().getVariables().put(TO_KEY, extractCurrencyFromMessage(payload.getContext()));
    }

    protected String getTo(StateContext<State, Event> context) {
        return (String) context.getExtendedState().getVariables().get(TO_KEY);
    }

    private String extractCurrencyFromMessage(String text) {
        return text.split(" ")[1];
    }

    protected List<List<InlineKeyboardButton>> getKeyboardMarkupExcludeCurrency(State state, Direction direction, List<String> currenciesToExclude) {
        List<List<InlineKeyboardButton>> keyboardMarkup = getKeyboardMarkup(state, direction);
        return keyboardMarkup.stream()
                .flatMap(inlineKeyboardButtons -> {
                    return inlineKeyboardButtons.stream()
                            .filter(inlineKeyboardButton -> !currenciesToExclude.contains(inlineKeyboardButton.getText()));
                }).map(Collections::singletonList)
                .collect(Collectors.toList());
    }

    protected List<List<InlineKeyboardButton>> getKeyboardMarkup(State state, Direction direction) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        banknotes.forEach(banknote -> {
            List<InlineKeyboardButton> buttonRow = Collections.singletonList(
                    InlineKeyboardButton.builder()
                            .text(banknote)
                            .callbackData(state + ";" + direction + " " + banknote)
                            .build()
            );
            buttons.add(buttonRow);
        });
        buttons.add(Collections.singletonList(InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData(Event.MAIN_MENU.toString())
                .build()));
        return buttons;
    }

    protected enum Direction {
        FROM, TO
    }

}
