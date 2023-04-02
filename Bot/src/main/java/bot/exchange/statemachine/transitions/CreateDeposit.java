package bot.exchange.statemachine.transitions;

import bot.exchange.service.rest.DepositHttpService;
import bot.exchange.statemachine.Event;
import bot.exchange.statemachine.Payload;
import bot.exchange.statemachine.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WithStateMachine
public class CreateDeposit extends Transition {

    @Autowired
    private MainMenu mainMenu;
    @Autowired
    private DepositHttpService depositHttpService;

    private List<String> banknotes = Arrays.asList("USD", "UAH", "EUR", "PLN");

    @Override
    public void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception {
        transitions.withExternal()
                .source(State.MAIN_MENU)
                .target(State.CREATE_DEPOSIT)
                .event(Event.CREATE_DEPOSIT)
                .action(this)
                .and()
                .withExternal()
                .source(State.CREATE_DEPOSIT)
                .target(State.MAIN_MENU)
                .event(Event.MAIN_MENU)
                .action(mainMenu);
    }

    @Override
    public State getState() {
        return State.CREATE_DEPOSIT;
    }

    @Override
    public void execute(StateContext<State, Event> context) {
        Payload payload = (Payload) context.getExtendedState().getVariables().get("payload");
        if (payload.isCallback()) {
            displayButtons(context);
        } else {
            sendDepositRequest(context);
        }
    }

    private void sendDepositRequest(StateContext<State, Event> context) {
        Payload payload = getPayload(context);
        List<List<InlineKeyboardButton>> buttons = (List<List<InlineKeyboardButton>>) context.getExtendedState().getVariables().get("depositKeys");

        String currencyToDeposit = null;
        for (List<InlineKeyboardButton> row : buttons) {
            for (InlineKeyboardButton inlineKeyboardButton : row) {
                if (inlineKeyboardButton.getText().contains("✅")) {
                    currencyToDeposit = inlineKeyboardButton.getCallbackData().split(";")[1];
                }
            }
        }

        String phone = getPhone(context);
        String amount = payload.getContext();
        boolean isCreated = depositHttpService.createDeposit(phone, currencyToDeposit, amount);

        String response = null;

        if (isCreated) {
            response = "Запит на депозит " + amount + " " + currencyToDeposit + " був вдало створен.";
        }else{
            response = "Помилка створення депозиту";
        }

        List<List<InlineKeyboardButton>> backButton = Arrays.asList(Arrays.asList(InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData(Event.MAIN_MENU.toString())
                .build()));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(backButton);
        botService.sendMessage(payload.getChatId(), response, inlineKeyboardMarkup);
    }

    private void displayButtons(StateContext<State, Event> context) {
        Payload payload = getPayload(context);
        List<List<InlineKeyboardButton>> currencyButtons = null;


        if (context.getExtendedState().getVariables().containsKey("depositKeys")) {
            currencyButtons = ((List<List<InlineKeyboardButton>>) context.getExtendedState().getVariables().get("depositKeys"));
        } else {
            currencyButtons = getCurrencyButtons();
        }

        if (payload.getContext() != null) {
            currencyButtons = updateCurrencyButtons(currencyButtons, payload.getContext());
        }

        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboard(currencyButtons)
                .build();

        if (payload.getContext() != null) {
            botService.editMessage(
                    EditMessageReplyMarkup.builder()
                            .chatId(payload.getChatId())
                            .messageId(payload.getMessageId())
                            .replyMarkup(keyboardMarkup)
                            .build()
            );
        } else {
            context.getExtendedState().getVariables().put("depositKeys", currencyButtons);
            botService.sendMessage(
                    payload.getChatId(),
                    "<< Депозит >>",
                    keyboardMarkup
            );
        }
    }

    private List<List<InlineKeyboardButton>> updateCurrencyButtons(List<List<InlineKeyboardButton>> buttons, String buttonCallback) {
        for (List<InlineKeyboardButton> row : buttons) {
            for (InlineKeyboardButton col : row) {
                if (col.getCallbackData().contains(buttonCallback)) {
                    col.setText(col.getText() + " ✅");
                } else {
                    col.setText(col.getText());
                }
            }
        }
        return buttons;
    }

    private List<List<InlineKeyboardButton>> getCurrencyButtons() {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();
        banknotes.forEach(banknote -> {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(banknote)
                    .callbackData(";" + banknote)
                    .build();
            buttonRow.add(button);
        });
        buttons.add(buttonRow);

        buttons.add(Arrays.asList(InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData(Event.MAIN_MENU.toString())
                .build()));
        return buttons;
    }

}
