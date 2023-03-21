package bot.commands.impl;

import bot.commands.BaseCommand;
import bot.commands.Command;
import bot.commands.CommandPayload;
import bot.exchange.service.rest.ExchangeHttpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProlongOperationCommand extends BaseCommand {

    @Autowired
    private ExchangeHttpService exchangeHttpService;

    @Override
    public Command getCommand() {
        return Command.PROLONG_OPERATION;
    }

    @Override
    public void process(CommandPayload commandPayload) {
        String[] split = commandPayload.getData().split(",");
        int code = Integer.parseInt(split[0]);
        String phone = split[1];

        boolean status = exchangeHttpService.prolongOperation(code, phone);

        String chatId = commandPayload.getChatId();
        if (status) {
            botService.sendMessage(chatId, "Операцію подовжено на 1 годину.");
        } else {
            botService.sendMessage(chatId, "Помилка подовження операції. Зв'яжіться з менеджером.");
        }
    }
}
