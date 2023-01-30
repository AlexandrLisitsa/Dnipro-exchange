package bot.exchange.statemachine.transitions.exchange;

import bot.exchange.service.rest.ExchangeHttpService;
import bot.exchange.statemachine.Event;
import bot.exchange.statemachine.State;
import bot.exchange.statemachine.transitions.Transition;
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
                .source(State.PRE_CONFIRM_EXCHANGE)
                .event(Event.CONFIRM_EXCHANGE)
                .target(State.CONFIRM_EXCHANGE)
                .action(this);
    }

    @Override
    public State getState() {
        return State.CONFIRM_EXCHANGE;
    }

    @Override
    public void execute(StateContext<State, Event> context) {

        ExchangeHttpService.CommitExchangeResponse exchangeResponse = (ExchangeHttpService.CommitExchangeResponse) context.getExtendedState().getVariables().get("exchange");

        Object exchangerId = context.getExtendedState().getVariables().get("exchangerId");
        Object isEnough = context.getExtendedState().getVariables().get("isEnough");

        if (exchangeHttpService.confirmExchange(exchangeResponse.getOperation(), (Integer) exchangerId, (Boolean) isEnough)) {
            String confirmationMessage = getConfirmationMessage(exchangeResponse);
            goToMainMenu(context, confirmationMessage);
        } else {
            goToMainMenu(context, "Помилка підтвердження операції.");
        }
    }

    private String getConfirmationMessage(ExchangeHttpService.CommitExchangeResponse exchangeResponse) {
        StringBuilder confirmMessage = new StringBuilder();
        confirmMessage.append("Операція успішно підтверджена.").append("\n");
        confirmMessage.append("Ваш код: ").append("[Api code]").append("\n\n");
        confirmMessage.append("Курс зафіксовано до ").append("[time from server]")
                .append(", після чого обмін буде виконано по поточному курсу на час здійснення операції.");

        return confirmMessage.toString();
    }
}
