package exchange.statemachine.transitions;

import exchange.statemachine.Event;
import exchange.statemachine.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@WithStateMachine
public class MyRates extends Transition {

    @Autowired
    private MainMenu mainMenu;

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

    }
}
