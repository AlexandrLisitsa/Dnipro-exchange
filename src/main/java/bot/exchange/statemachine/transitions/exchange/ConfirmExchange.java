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

@Slf4j
@WithStateMachine
public class ConfirmExchange extends Transition {

    @Autowired
    private ExchangeHttpService exchangeHttpService;

    @Override
    public void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception {
        transitions.withExternal()
                .source(State.EXCHANGE)
                .target(State.CONFIRM_EXCHANGE)
                .event(Event.CONFIRM_EXCHANGE)
                .action(this)
                .and()
                .withExternal()
                .source(State.CONFIRM_EXCHANGE)
                .target(State.MAIN_MENU)
                .event(Event.MAIN_MENU)
                .action(mainMenu);
    }

    @Override
    public State getState() {
        return State.CONFIRM_EXCHANGE;
    }

    @Override
    public void execute(StateContext<State, Event> context) {
        ExchangeHttpService.CommitExchangeResponse exchangeResponse = (ExchangeHttpService.CommitExchangeResponse) context.getExtendedState().getVariables().get("exchange");

        Payload payload = getPayload(context);
        String[] split = payload.getContext().split(",");
        int exchangerId = Integer.parseInt(split[0]);
        boolean isEnough = Boolean.parseBoolean(split[1]);

        if (exchangeHttpService.confirmExchange(exchangeResponse.getOperation(), exchangerId, isEnough)) {
            goToMainMenu(context, "Операція успішно підтвердженна.");
        } else {
            goToMainMenu(context, "Помилка підтвердження операції.");
        }
    }
}
