package bot.exchange.statemachine.transitions;

import bot.exchange.service.rest.ClientHttpService;
import bot.exchange.statemachine.Event;
import bot.exchange.statemachine.State;
import bot.exchange.telegram.Icons;
import bot.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@WithStateMachine
public class MainMenu extends Transition {

    @Autowired
    private ClientHttpService clientHttpService;

    private DateTimeFormatter dateTemplate = DateTimeFormatter.ofPattern("dd.MM.yyyy на HH.mm");

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

        String phone = getPhone(context);
        String greetingMessage = getGreetingMessage(phone);

       /* InlineKeyboardButton myRates = new InlineKeyboardButton();
        myRates.setText("Мій курс");
        myRates.setCallbackData(Event.MY_RATES.toString());

        InlineKeyboardButton rates = new InlineKeyboardButton();
        rates.setText("Курси валют");
        rates.setCallbackData(Event.BANK_RATES.toString());*/

        InlineKeyboardButton exchange = new InlineKeyboardButton();
        exchange.setText("Обміняти");
        exchange.setCallbackData(Event.FROM_CURRENCY.toString());

        /*InlineKeyboardButton depositRules = new InlineKeyboardButton();
        depositRules.setText("Правила депозита");
        depositRules.setCallbackData(Event.DEPOSIT_RULES.toString());

        InlineKeyboardButton deposit = new InlineKeyboardButton();
        deposit.setText("Депозит");
        deposit.setCallbackData(Event.CREATE_DEPOSIT.toString());*/

        /*
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
                Collections.singletonList(exchange)
                /*Collections.singletonList(myRates),
                Collections.singletonList(rates),
                ,
                Collections.singletonList(depositRules),
                Collections.singletonList(deposit)*/
               /* Collections.singletonList(deposit),
                Collections.singletonList(cryptoExchange),
                Collections.singletonList(inviteFriend),
                Collections.singletonList(balance),
                Collections.singletonList(exchangeHistory),
                */);
        inlineKeyboardMarkup.setKeyboard(buttons);

        long chatId = getChatId(context);

        botService.sendMessage(String.valueOf(chatId), greetingMessage, inlineKeyboardMarkup);
    }

    private String getGreetingMessage(String phoneNumber) {
        Optional<ClientHttpService.Client> clientInfo = clientHttpService.getClientInfo(phoneNumber);
        StringBuilder message = new StringBuilder();

        message.append("\uD83D\uDCC6 ").append(DateTimeUtils.getFormattedKievDateTime()).append(" \uD83D\uDCB1 \n\n");

        message.append("\uD83D\uDCB0\uD83D\uDCB0\uD83D\uDCB0 Найвигідніший курс у місті Дніпро! \uD83D\uDCB0\uD83D\uDCB0\uD83D\uDCB0").append("\n\n");

        message.append("\uD83D\uDCB2Доллар нового зразка +0.2 грн до курсу\uD83D\uDCB2").append("\n");
        message.append("\uD83D\uDCB2Доллар 1996-1999 року за курсом НБУ\uD83D\uDCB2\n").append("\n\n");

        message.append("‼️Ваш особистий курс:").append("\n\n");

        clientInfo.ifPresent(client -> {
            client.getRates().forEach((level, discounts) -> {
                appendDiscountMessage(message, discounts);
            });
        });

        message.append("\n");
        message.append("\uD83D\uDCC8\uD83D\uDCC9 Курс залежить від суми обміну і вашої персональної знижки. Натисніть обмін і введіть бажану суму, щоб дізнатись остаточний курс. Або тисни на Наші правила.");
        message.append("\n\n");

        message.append("\uD83D\uDCB8 Приймаємо зношені купюри з комісією від 15%.").append("\n");
        message.append("\uD83D\uDC49 Міняємо долари старого зразка на нові купюри \uD83D\uDCB5");
        message.append("\n\n");

        message.append("✅ Наші переваги:\n" +
                "- безпека та конфедиційність\n" +
                "- видача чека операції\n" +
                "- доставка валюти за запитом\n" +
                "- розвинута мережа обмінів валют").append("\n\n");

        message.append("Залишились питання, зв’яжіться з відділом продажів:\n" +
                "\uD83D\uDCF1  +38 067 106 00 55\n" +
                "\uD83D\uDD70 Графік роботи: 8:30-19:00.").append("\n");

        return message.toString();
    }

    private void appendDiscountMessage(StringBuilder message, ClientHttpService.Discounts discounts) {
        if (discounts.getFrom() == null) {
            message.append("При обміні до $").append(discounts.getTo()).append(":\n");
        } else if (discounts.getTo() == null) {
            message.append("При обміні від $").append(" - Індивідуальний курс").append(":\n");
            return;
        } else {
            message.append("При обміні від $").append(discounts.getFrom()).append(" до $").append(discounts.getTo()).append(":\n");
        }
        discounts.getRates().forEach((currency, rates) -> {
            String currencyUpperCase = currency.toUpperCase();
            message.append(Icons.CURRENCY_ICONS.get(currencyUpperCase)).append(currencyUpperCase)
                    .append(" ")
                    .append(rates.getBuy())
                    .append("/")
                    .append(rates.getSell()).append("\n");
        });
        message.append("\n");
    }

}
