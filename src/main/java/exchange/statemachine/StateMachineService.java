package exchange.statemachine;

import exchange.statemachine.persister.StateMachinePersist;
import exchange.statemachine.transitions.Transition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StateMachineService {

    @Autowired
    private StateMachinePersist persist;
    @Autowired
    private StateMachineFactory<State, Event> stateMachineFactory;
    @Autowired
    private List<Transition> transitions;

    public void initStateMachine(String userId, long chatId) {
        StateMachine<State, Event> stateMachine = stateMachineFactory.getStateMachine();

        stateMachine.getExtendedState().getVariables().put("chatId", chatId);

        stateMachine.sendEvent(
                Mono.just(MessageBuilder.withPayload(Event.MAIN_MENU).build())).subscribe();
        persistStateMachine(stateMachine, userId);
    }

    public void changeState(Payload payload) {
        if (payload.getEvent() == null) {
            updateState(payload);
        } else {
            StateMachine<State, Event> stateMachine = restoreStateMachine(payload.getUserId());
            stateMachine.getExtendedState().getVariables().put("payload", payload);
            stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(payload.getEvent()).build()))
                    .subscribe();
            persistStateMachine(stateMachine, payload.getUserId());
        }
    }

    private void updateState(Payload payload) {
        StateMachine<State, Event> stateMachine = restoreStateMachine(payload.getUserId());
        stateMachine.getExtendedState().getVariables().put("payload", payload);

        State currentState = stateMachine.getState().getId();
        Optional<Transition> machineState = transitions.stream().filter(transition -> transition.getState() == currentState)
                .findFirst();
        if (machineState.isPresent()) {
            Transition transition = machineState.get();
            DefaultStateContext<State, Event> stateEventDefaultStateContext = new DefaultStateContext<>(
                    null,
                    null,
                    null,
                    stateMachine.getExtendedState(),
                    null,
                    stateMachine,
                    null,
                    null,
                    null);
            transition.execute(stateEventDefaultStateContext);
        }
    }

    private StateMachine<State, Event> restoreStateMachine(String userId) {
        try {
            return persist.restore(
                    stateMachineFactory.getStateMachine(), userId);
        } catch (Exception e) {
            log.error("Exception restoring state machine", e);
            throw new RuntimeException(e);
        }
    }

    private void persistStateMachine(StateMachine<State, Event> stateMachine, String userId) {
        try {
            persist.persist(stateMachine, userId);
        } catch (Exception e) {
            log.error("Error persisting state machine", e);
        }
    }

}