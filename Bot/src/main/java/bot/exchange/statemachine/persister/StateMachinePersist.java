package bot.exchange.statemachine.persister;

import bot.exchange.statemachine.Event;
import bot.exchange.statemachine.State;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.persist.AbstractStateMachinePersister;
import org.springframework.stereotype.Component;

@Component
public class StateMachinePersist extends AbstractStateMachinePersister<State, Event, String> {

    public StateMachinePersist(org.springframework.statemachine.StateMachinePersist<State, Event, String> stateMachinePersist) {
        super(stateMachinePersist);
    }

    @Override
    protected StateMachineContext<State, Event> buildStateMachineContext(
            StateMachine<State, Event> stateMachine) {
        return super.buildStateMachineContext(stateMachine);
    }
}