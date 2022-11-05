package exchange.statemachine.transitions;

import exchange.service.rest.ClientHttpService;
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
import java.util.Map;

@WithStateMachine
public class MyRates extends Transition {

    @Autowired
    private MainMenu mainMenu;
    @Autowired
    private ClientHttpService clientHttpService;

    @Override
    public void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception {
        transitions.withExternal()
                .source(State.MAIN_MENU)
                .target(State.MY_RATES)
                .event(Event.MY_RATES)
                .action(this)
                .and()
                .withExternal()
                .source(State.MY_RATES)
                .target(State.MAIN_MENU)
                .event(Event.MAIN_MENU)
                .action(mainMenu);
    }

    @Override
    public State getState() {
        return State.MY_RATES;
    }

    @Override
    public void execute(StateContext<State, Event> context) {
        String phone = (String) context.getExtendedState().getVariables().get("phone");
        Map<String, ClientHttpService.CurrencyValue> clientRates = clientHttpService.getClientRates(phone);

        StringBuilder ratesMessage = new StringBuilder("<<< Мои курсы >>>").append("\n\n");
        clientRates.forEach((currency, values) -> {
            ratesMessage.append("         ").append(currency.toUpperCase()).append("\n");
            ratesMessage.append("Покупка: ").append(values.getBuy()).append("\n");
            ratesMessage.append("Продажа: ").append(values.getSell()).append("\n\n");
        });

        InlineKeyboardButton mainMenuButton = InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData(Event.MAIN_MENU.toString())
                .build();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(Collections.singletonList(Collections.singletonList(mainMenuButton)));

        Payload payload = (Payload) context.getExtendedState().getVariables().get("payload");

        botService.sendMessage(payload.getChatId(), ratesMessage.toString(), markup);
    }
}
