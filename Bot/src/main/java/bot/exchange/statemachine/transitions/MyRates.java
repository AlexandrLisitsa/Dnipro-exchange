package bot.exchange.statemachine.transitions;

import bot.exchange.service.rest.ClientHttpService;
import bot.exchange.statemachine.Event;
import bot.exchange.statemachine.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

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
        Optional<ClientHttpService.Client> client = clientHttpService.getClientInfo(phone);
        if (client.isPresent()) {
            displayRates(client.get().getRates(), getPayload(context).getChatId());
        } else {
            goToMainMenu(context, "Кліент не знайден");
        }
    }

    private void displayRates(Map<String, ClientHttpService.Discounts> clientRates, String chatId) {

        StringBuilder ratesMessage = new StringBuilder("<<< Мої курси >>>").append("\n\n");
        clientRates.forEach((currency, values) -> {
            ratesMessage.append("         ").append(currency.toUpperCase()).append("\n");
            // TODO: 03.04.2023 I think that we need to remove this state
            //   ratesMessage.append("Купівля: ").append(values.getBuy()).append("\n");
            //  ratesMessage.append("Продаж: ").append(values.getSell()).append("\n\n");
        });

        InlineKeyboardButton mainMenuButton = InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData(Event.MAIN_MENU.toString())
                .build();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(Collections.singletonList(Collections.singletonList(mainMenuButton)));

        botService.sendMessage(chatId, ratesMessage.toString(), markup);
    }

}
