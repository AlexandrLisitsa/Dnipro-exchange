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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;

public abstract class Transition implements Action<State, Event> {

    private MainMenu mainMenu;

    @Autowired
    public void setMainMenu(@Lazy MainMenu mainMenu) {
        this.mainMenu = mainMenu;
    }

    public BotService botService;

    @Autowired
    public void setBotService(@Lazy BotService botService) {
        this.botService = botService;
    }

    public abstract void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception;

    public abstract State getState();

    public Payload getPayload(StateContext<State, Event> context) {
        return (Payload) context.getExtendedState().getVariables().get("payload");
    }

    public String getPhone(StateContext<State, Event> context) {
        return (String) context.getExtendedState().getVariables().get("phone");
    }

    public void goToMainMenu(StateContext<State, Event> context, String message) {
        InlineKeyboardButton mainMenuButton = InlineKeyboardButton.builder()
                .text("Главное меню")
                .callbackData(Event.MAIN_MENU.toString())
                .build();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(Collections.singletonList(Collections.singletonList(mainMenuButton)));

        Payload payload = (Payload) context.getExtendedState().getVariables().get("payload");

        botService.sendMessage(payload.getChatId(), message, markup);
    }

}
