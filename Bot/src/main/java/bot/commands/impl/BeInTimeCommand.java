package bot.commands.impl;

import bot.commands.BaseCommand;
import bot.commands.Command;
import bot.commands.CommandPayload;
import org.springframework.stereotype.Component;

@Component
public class BeInTimeCommand extends BaseCommand {

    @Override
    public Command getCommand() {
        return Command.BE_IN_TIME;
    }

    @Override
    public void process(CommandPayload commandPayload) {
        botService.sendMessage(commandPayload.getChatId(), "Чекаємо вас!");
    }
}
