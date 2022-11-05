package exchange.statemachine.transitions;

import exchange.statemachine.Event;
import exchange.statemachine.State;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@WithStateMachine
public class MainMenu extends Transition {

    @Override
    public void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception {
        transitions.withExternal()
                .source(State.START)
                .target(State.MAIN_MENU)
                .event(Event.MAIN_MENU)
                .action(this);
    }

    @Override
    public State getState() {
        return State.MAIN_MENU;
    }

    @Override
    public void execute(StateContext<State, Event> context) {

        InlineKeyboardButton myRates = new InlineKeyboardButton();
        myRates.setText("Мой курс");
        myRates.setCallbackData(Event.MY_RATES.toString());

       /* InlineKeyboardButton exchange = new InlineKeyboardButton();
        exchange.setText("Обменять");
        exchange.setCallbackData(Event.EXCHANGE.toString());

        InlineKeyboardButton rates = new InlineKeyboardButton();
        rates.setText("Курсы валют");
        rates.setCallbackData(Event.RATES.toString());*/

        /*InlineKeyboardButton deposit = new InlineKeyboardButton();
        deposit.setText("Депозит");
        deposit.setCallbackData("1");

        InlineKeyboardButton rules = new InlineKeyboardButton();
        rules.setText("Правила");
        rules.setCallbackData("1");

        InlineKeyboardButton cryptoExchange = new InlineKeyboardButton();
        cryptoExchange.setText("Криптообмен");
        cryptoExchange.setCallbackData("1");

        InlineKeyboardButton inviteFriend = new InlineKeyboardButton();
        inviteFriend.setText("Пригласи друга");
        inviteFriend.setCallbackData("1");

        InlineKeyboardButton exchangeHistory = new InlineKeyboardButton();
        exchangeHistory.setText("История обменов");
        exchangeHistory.setCallbackData("1");

        InlineKeyboardButton balance = new InlineKeyboardButton();
        balance.setText("Ваш баланс");
        balance.setCallbackData("1");*/

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = Arrays.asList(
                //Collections.singletonList(exchange),
                 Collections.singletonList(myRates)
                //Collections.singletonList(rates)
               /* Collections.singletonList(deposit),
                Collections.singletonList(cryptoExchange),
                Collections.singletonList(inviteFriend),
                Collections.singletonList(balance),
                Collections.singletonList(exchangeHistory),
                Collections.singletonList(rules)*/);
        inlineKeyboardMarkup.setKeyboard(buttons);

        long chatId = (long) context.getExtendedState().getVariables().get("chatId");

        botService.sendMessage(String.valueOf(chatId), "<<< Главное меню >>>", inlineKeyboardMarkup);
    }

}
