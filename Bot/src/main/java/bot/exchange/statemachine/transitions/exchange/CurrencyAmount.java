package bot.exchange.statemachine.transitions.exchange;

import bot.exchange.service.rest.ExchangeHttpService;
import bot.exchange.statemachine.Event;
import bot.exchange.statemachine.Payload;
import bot.exchange.statemachine.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@WithStateMachine
public class CurrencyAmount extends CurrencyDirections {

    @Autowired
    private ExchangeHttpService exchangeHttpService;

    @Override
    public void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception {
        transitions.withExternal()
                .source(State.TO_CURRENCY)
                .target(State.CURRENCY_AMOUNT)
                .event(Event.CURRENCY_AMOUNT)
                .action(this)
                .and()
                .withExternal()
                .source(State.CURRENCY_AMOUNT)
                .target(State.MAIN_MENU)
                .event(Event.MAIN_MENU)
                .action(mainMenuAction);
    }

    @Override
    public State getState() {
        return State.CURRENCY_AMOUNT;
    }

    @Override
    public void execute(StateContext<State, Event> context) {
        Payload payload = getPayload(context);

        Optional<Double> amount = getAmount(payload.getContext());
        if (amount.isPresent()) {
            commitExchange(context, amount.get());
        } else {
            setTo(context);
            botService.sendMessage(payload.getChatId(), "Введіть суму у валюті обміну (доллар, євро або злотий)");
        }
    }

    private void commitExchange(StateContext<State, Event> context, Double amount) {
        String phone = getPhone(context);
        String direction = getDirection(context);
        String stringAmount = String.valueOf(amount);
        ExchangeHttpService.CommitExchangeResponse commitExchangeResponse = exchangeHttpService.commitExchange(
                phone,
                direction,
                stringAmount);

        processExchangeResponse(commitExchangeResponse, context);
    }

    private void processExchangeResponse(ExchangeHttpService.CommitExchangeResponse response, StateContext<State, Event> context) {
        List<ExchangeHttpService.Exchanger> readyExchangers = getAvailableExchangers(response);

        if (readyExchangers.isEmpty()) {
            sendIsNotEnoughMessage(context);
        } else {
            context.getExtendedState().getVariables().put("exchange", response);
            sendLocationsToExchange(context, response);
        }
    }

    private void sendLocationsToExchange(StateContext<State, Event> context, ExchangeHttpService.CommitExchangeResponse response) {
        String exchangeMessage = getExchangeMessage(response.getOperation());
        InlineKeyboardMarkup availableLocations = getAvailableLocations(response.getAvaliable_exchangers());
        long chatId = getChatId(context);

        botService.sendMessage(String.valueOf(chatId), exchangeMessage, availableLocations);
    }

    private InlineKeyboardMarkup getAvailableLocations(List<ExchangeHttpService.Exchanger> avaliable_exchangers) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> locations = avaliable_exchangers.stream()
                .map(exchanger -> {
                    InlineKeyboardButton location = InlineKeyboardButton.builder()
                            .text(exchanger.getTitle())
                            .callbackData(Event.PRE_CONFIRM_EXCHANGE + ";" + exchanger.getId() + "," + exchanger.isEnough())
                            .build();
                    return Collections.singletonList(location);
                }).collect(Collectors.toList());

        inlineKeyboardMarkup.setKeyboard(locations);

        return inlineKeyboardMarkup;
    }

    private String getExchangeMessage(ExchangeHttpService.Operation operation) {
        String exchangeTo = operation.getDirection().split("->")[1];

        StringBuilder message = new StringBuilder("Операція\n");
        message.append(operation.getDirection()).append("\n");
        message.append("До видачі: ").append(operation.getReceive()).append(" ").append(exchangeTo).append("\n\n");
        message.append("Оберіть зручний пункт обміну валют для здійснення операції");

        return message.toString();
    }

    private void sendIsNotEnoughMessage(StateContext<State, Event> context) {
        goToMainMenu(context, "Нам потрібен деякий час для підготовки потрібної суми," +
                " з вами зв’яжеться наш менеджер для обговорення деталей операції");
    }

    private List<ExchangeHttpService.Exchanger> getAvailableExchangers(ExchangeHttpService.CommitExchangeResponse response) {
        return response.getAvaliable_exchangers().stream()
                .filter(ExchangeHttpService.Exchanger::isEnough)
                .collect(Collectors.toList());
    }

    private Optional<Double> getAmount(String string) {
        try {
            double amount = Double.parseDouble(string);
            return Optional.of(amount);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

}
