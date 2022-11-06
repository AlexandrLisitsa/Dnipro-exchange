package exchange.statemachine.transitions;

import exchange.statemachine.Event;
import exchange.statemachine.Payload;
import exchange.statemachine.State;
import exchange.telegram.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public abstract class Transition implements Action<State, Event> {

    public BotService botService;

    @Autowired
    public void setBotService(@Lazy BotService botService) {
        this.botService = botService;
    }

    public abstract void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception;

    public abstract State getState();

    public Payload getPayload(StateContext<State, Event> context){
        return (Payload) context.getExtendedState().getVariables().get("payload");
    }

}
