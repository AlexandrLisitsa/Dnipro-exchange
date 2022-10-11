package exchange.statemachine;

import exchange.statemachine.persister.StateMachinePersist;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class StateMachineService {

    @Autowired
    StateMachinePersist persist;
    @Autowired
    StateMachineFactory<State, Event> stateMachineFactory;

    public void initStateMachine(String userId, long chatId) {
        StateMachine<State, Event> stateMachine = stateMachineFactory.getStateMachine();

        stateMachine.getExtendedState().getVariables().put("chatId", chatId);

        stateMachine.sendEvent(
                Mono.just(MessageBuilder.withPayload(Event.MAIN_MENU).build())).subscribe();
        persistStateMachine(stateMachine, userId);
    }

    public void changeState(Payload payload) {
        StateMachine<State, Event> stateMachine = restoreStateMachine(payload.getUserId());
        stateMachine.getExtendedState().getVariables().put("payload", payload);
        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(payload.getEvent()).build()))
                .subscribe();
        persistStateMachine(stateMachine, payload.getUserId());
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