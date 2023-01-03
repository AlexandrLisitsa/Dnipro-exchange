package bot.exchange.statemachine.transitions.exchange;

import bot.exchange.service.rest.ExchangeHttpService;
import bot.exchange.statemachine.Event;
import bot.exchange.statemachine.Payload;
import bot.exchange.statemachine.State;
import bot.exchange.statemachine.transitions.MainMenu;
import bot.exchange.statemachine.transitions.Transition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.stream.Collectors;

@WithStateMachine
public class Exchange extends Transition {

    @Autowired
    private ExchangeHttpService exchangeHttpService;
    @Autowired
    private MainMenu mainMenuAction;
    private List<String> banknotes = Arrays.asList("USD", "UAH", "EUR", "PLN");

    @Override
    public void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception {
        transitions.withExternal()
                .source(State.MAIN_MENU)
                .action(this)
                .event(Event.EXCHANGE)
                .target(State.EXCHANGE)
                .and()
                .withExternal()
                .source(State.EXCHANGE)
                .target(State.MAIN_MENU)
                .event(Event.MAIN_MENU)
                .action(mainMenuAction);
    }

    @Override
    public State getState() {
        return State.EXCHANGE;
    }

    @Override
    public void execute(StateContext<State, Event> context) {
        Payload payload = (Payload) context.getExtendedState().getVariables().get("payload");
        if (payload.isCallback()) {
            displayButtons(context);
        } else {
            sendExchangeRequest(context);
        }
    }

    private void sendExchangeRequest(StateContext<State, Event> context) {
        Payload payload = (Payload) context.getExtendedState().getVariables().get("payload");
        List<List<InlineKeyboardButton>> buttons = (List<List<InlineKeyboardButton>>) context.getExtendedState().getVariables().get("exchangeKeys");
        String from = "";
        String to = "";
        for (List<InlineKeyboardButton> col : buttons) {
            for (InlineKeyboardButton row : col) {
                if (row.getCallbackData().contains("from") && row.getText().contains("✅")) {
                    from = row.getText().split(" ")[0];
                }
                if (row.getCallbackData().contains("to") && row.getText().contains("✅")) {
                    to = row.getText().split(" ")[0];
                }
            }
        }

        String direction = from + "->" + to;
        ExchangeHttpService.CommitExchangeResponse commitExchangeResponse = exchangeHttpService.commitExchange(getPhone(context), direction, payload.getContext());
        context.getExtendedState().getVariables().put("exchange", commitExchangeResponse);
        handleExchangeResponse(commitExchangeResponse, from, to, context);
    }

    private void handleExchangeResponse(ExchangeHttpService.CommitExchangeResponse response, String from, String to, StateContext<State, Event> context) {
        String exchangeMessage = "Обмін " + getPayload(context).getContext() + " " + from + " на " + to;
        List<ExchangeHttpService.Exchanger> exchangers = response.getAvaliable_exchangers();
        if (exchangers.isEmpty()) {
            goToMainMenu(context, exchangeMessage + "\nЗапитуваємої суми немає в наявності, з Вами зв'яжуться.");
        } else {
            List<List<InlineKeyboardButton>> exchangerPoints = exchangers.stream().map(exchanger -> {
                if (exchanger.isEnough()) {
                    InlineKeyboardButton exchangerButton = InlineKeyboardButton.builder()
                            .callbackData(Event.CONFIRM_EXCHANGE + ";" + exchanger.getId())
                            .text(buildExchangerMessage(exchanger))
                            .build();
                    return Collections.singletonList(exchangerButton);
                } else {
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
            InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().keyboard(exchangerPoints).build();

            if (exchangerPoints.isEmpty()) {
                botService.sendMessage(getPayload(context).getChatId(), "Наразі в обраному обміннику недостатньо коштів для здійснення операції." +
                        " Ви зможете здійснити операцію сьогодні з 18:00 до 19:30 або завтра з 08:30 до 12:00.\n" +
                        "Або оберіть інший обмінник. ");
            } else {
                botService.sendMessage(getPayload(context).getChatId(), buildExchangeConfirmMessage(response.getOperation()), markup);
            }
        }
    }

    private String buildExchangeConfirmMessage(ExchangeHttpService.Operation operation) {
        StringBuilder operationText = new StringBuilder();

        operationText.append("Операція: ").append("\n");
        operationText.append(operation.getDirection()).append(" До видачі: ").append(operation.getReceive()).append("\n\n");
        operationText.append("Оберіть зручний обмінник для здійснення операції");
        return operationText.toString();
    }

    private String buildExchangerMessage(ExchangeHttpService.Exchanger exchanger) {
        StringBuilder stringBuilder = new StringBuilder(exchanger.getTitle()).append("\n");
        if (!exchanger.isEnough()) {
            ExchangeHttpService.Time timeBounds = exchanger.getTime_bounds();
            if (timeBounds.isToday()) {
                stringBuilder
                        .append("Сьогодні з ")
                        .append(timeBounds.getFrom())
                        .append(" по ")
                        .append(timeBounds.getTo());
            }
        }
        return stringBuilder.toString();
    }

    private void displayButtons(StateContext<State, Event> context) {
        Payload payload = (Payload) context.getExtendedState().getVariables().get("payload");

        List<List<InlineKeyboardButton>> keyboardButtons;

        if (context.getExtendedState().getVariables().containsKey("exchangeKeys")) {
            keyboardButtons = ((List<List<InlineKeyboardButton>>) context.getExtendedState().getVariables().get("exchangeKeys"));
        } else {
            keyboardButtons = getKeyboardMarkup();
        }

        if (payload.getContext() != null) {
            keyboardButtons = updateKeyboardButtons(keyboardButtons, payload.getContext());
        }

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboardButtons);

        if (payload.getContext() != null) {
            botService.editMessage(
                    EditMessageReplyMarkup.builder()
                            .chatId(payload.getChatId())
                            .messageId(payload.getMessageId())
                            .replyMarkup(inlineKeyboardMarkup)
                            .build()
            );
        } else {
            context.getExtendedState().getVariables().put("exchangeKeys", keyboardButtons);
            botService.sendMessage(
                    payload.getChatId(),
                    "<< Обмін >>",
                    inlineKeyboardMarkup
            );
        }
    }

    private List<List<InlineKeyboardButton>> updateKeyboardButtons(List<List<InlineKeyboardButton>> buttons, String buttonCallback) {
        String exchangeSide = buttonCallback.split(" ")[0];
        for (List<InlineKeyboardButton> row : buttons) {
            for (InlineKeyboardButton col : row) {
                if (col.getCallbackData().contains(buttonCallback) && col.getCallbackData().contains(exchangeSide)) {
                    col.setText(col.getText() + " ✅");
                } else if (col.getCallbackData().contains(exchangeSide) && !col.getCallbackData().contains(buttonCallback)) {
                    col.setText(col.getText().split(" ")[0]);
                } else {
                    col.setText(col.getText());
                }
            }
        }
        return buttons;
    }

    private List<List<InlineKeyboardButton>> getKeyboardMarkup() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        banknotes.forEach(banknote -> {
            List<InlineKeyboardButton> buttonRow = Arrays.asList(
                    InlineKeyboardButton.builder()
                            .text(banknote)
                            .callbackData(";from " + banknote)
                            .build(),
                    InlineKeyboardButton.builder()
                            .text(banknote)
                            .callbackData(";to " + banknote)
                            .build()
            );
            buttons.add(buttonRow);
        });
        buttons.add(Arrays.asList(InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData(Event.MAIN_MENU.toString())
                .build()));
        return buttons;
    }

}
