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

        sendLocationsToExchange(context, commitExchangeResponse);
    }

    private void sendLocationsToExchange(StateContext<State, Event> context, ExchangeHttpService.CommitExchangeResponse response) {
        context.getExtendedState().getVariables().put("exchange", response);
        String exchangeMessage = getExchangeMessage(response);
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

    private String getExchangeMessage(ExchangeHttpService.CommitExchangeResponse response) {

        ExchangeHttpService.Operation operation = response.getOperation();

        List<ExchangeHttpService.Exchanger> availableExchangers = getAvailableExchangers(response);

        StringBuilder message;

        if (availableExchangers.isEmpty()) {
            message = new StringBuilder("Нам потрібен деякий час для підготовки потрібної суми, " +
                    "з вами зв’яжеться наш менеджер для обговорення деталей операції\n\n");
        } else {
            message = new StringBuilder();
        }

        message.append("Операція\n");
        message.append(operation.getDirection()).append("\n");
        message.append("Отримуєте: ").append(operation.getReceive()).append("\n");
        message.append("Віддаєте: ").append(operation.getAmount()).append("\n\n");
        message.append("Оберіть зручний пункт обміну валют для здійснення операції");

        return message.toString();
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
