package exchange.statemachine.transitions;

import exchange.statemachine.Event;
import exchange.statemachine.State;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@WithStateMachine
public class ActualCurrency extends Transition {

    @Override
    public void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception {

    }

    @Override
    public void execute(StateContext<State, Event> context) {

    }
}
