package exchange.statemachine.transitions;

import exchange.service.rest.DepositHttpService;
import exchange.statemachine.Event;
import exchange.statemachine.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@WithStateMachine
public class DepositRules extends Transition {

    @Autowired
    private DepositHttpService depositHttpService;

    @Override
    public void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception {
        transitions.withExternal()
                .source(State.MAIN_MENU)
                .target(State.DEPOSIT_RULES)
                .event(Event.DEPOSIT_RULES)
                .action(this)
                .and()
                .withExternal()
                .source(State.DEPOSIT_RULES)
                .target(State.MAIN_MENU)
                .event(Event.MAIN_MENU)
                .action(mainMenu);
    }

    @Override
    public State getState() {
        return State.DEPOSIT_RULES;
    }

    @Override
    public void execute(StateContext<State, Event> context) {
        String depositRules = depositHttpService.getDepositRules();
        goToMainMenu(context, depositRules);
    }
}
