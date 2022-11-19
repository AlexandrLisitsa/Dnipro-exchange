package exchange.statemachine.transitions.exchange;

import exchange.service.rest.ExchangeHttpService;
import exchange.statemachine.Event;
import exchange.statemachine.State;
import exchange.statemachine.transitions.Transition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

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
        if (exchangeHttpService.confirmExchange(exchangeResponse.getOperation(), Integer.parseInt(getPayload(context).getContext()))) {
            goToMainMenu(context, "Операция успешно подтверждена.");
        } else {
            goToMainMenu(context, "Ошибка подтверждения операции");
        }
    }
}
