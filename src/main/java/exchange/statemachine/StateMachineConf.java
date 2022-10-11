package exchange.statemachine;

import exchange.statemachine.transitions.Transition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;
import java.util.List;

@Configuration
@EnableStateMachineFactory
public class StateMachineConf extends EnumStateMachineConfigurerAdapter<State, Event> {

    @Autowired
    List<Transition> transitions;

    @Override
    public void configure(final StateMachineStateConfigurer<State, Event> states)
            throws Exception {
        states.withStates().initial(State.START).states(EnumSet.allOf(State.class));
    }

    @Override
    public void configure(
            final StateMachineConfigurationConfigurer<State, Event> config)
            throws Exception {
        config
                .withConfiguration()
                .autoStartup(true);
    }

    @Override
    public void configure(final StateMachineTransitionConfigurer<State, Event> transitions) {
        this.transitions.forEach(transition -> {
            try {
                transition.configure(transitions);
            } catch (Exception e) {
                throw new StateMachineException("Transition configure exception");
            }
        });
    }

}