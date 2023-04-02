package bot.commands.impl;

import bot.commands.BaseCommand;
import bot.commands.Command;
import bot.commands.CommandPayload;
import bot.exchange.service.rest.ExchangeHttpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RejectOperation extends BaseCommand {

    @Autowired
    private ExchangeHttpService exchangeHttpService;

    @Override
    public Command getCommand() {
        return Command.REJECT_OPERATION;
    }

    @Override
    public void process(CommandPayload commandPayload) {
        String[] split = commandPayload.getData().split(",");
        String code = split[0];
        String phone = split[1];

        boolean status = exchangeHttpService.rejectOperation(Integer.parseInt(code), phone);
        String chatId = commandPayload.getChatId();
        if (status) {
            botService.sendMessage(chatId, "Операція скасована.");
        } else {
            botService.sendMessage(chatId, "Помилка скасування операції. Зв'яжіться з менеджером.");
        }
    }
}
