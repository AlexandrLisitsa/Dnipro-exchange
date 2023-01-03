package bot.exchange.statemachine.persister;

import bot.exchange.statemachine.Event;
import bot.exchange.statemachine.State;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class CustomStateMachinePersist implements StateMachinePersist<State, Event, String> {

    private final HashMap<String, StateMachineContext<State, Event>> contexts = new HashMap<>();

    @Override
    public void write(final StateMachineContext<State, Event> context, String contextObj) {
        contexts.put(contextObj, context);
    }

    @Override
    public StateMachineContext<State, Event> read(final String contextObj) {
        return contexts.get(contextObj);
    }
}
