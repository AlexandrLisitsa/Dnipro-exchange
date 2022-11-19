package exchange.statemachine.transitions.exchange;

import exchange.service.rest.ExchangeHttpService;
import exchange.statemachine.Event;
import exchange.statemachine.Payload;
import exchange.statemachine.State;
import exchange.statemachine.transitions.MainMenu;
import exchange.statemachine.transitions.Transition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
        String exchangeMessage = "Обмен " + getPayload(context).getContext() + " " + from + " на " + to;
        List<ExchangeHttpService.Exchanger> exchangers = response.getAvaliable_exchangers();
        if (exchangers.isEmpty()) {
            goToMainMenu(context, exchangeMessage + "\nЗапрашиваемой суммы нет в наличии, с Вами свяжутся.");
        } else {
            List<List<InlineKeyboardButton>> exchangerPoints = exchangers.stream().map(exchanger -> {
                InlineKeyboardButton exchangerButton = InlineKeyboardButton.builder()
                        .callbackData(Event.CONFIRM_EXCHANGE + ";" + exchanger.getId())
                        .text(exchanger.getTitle())
                        .build();
                return Collections.singletonList(exchangerButton);
            }).collect(Collectors.toList());
            InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().keyboard(exchangerPoints).build();
            botService.sendMessage(getPayload(context).getChatId(), "Выберите кассу для обмена", markup);
        }
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
                    "<< Обмен >>",
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
