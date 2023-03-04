package bot.commands;

import bot.exchange.telegram.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

public abstract class BaseCommand {

    protected BotService botService;

    @Autowired
    public void setBotService(@Lazy BotService botService) {
        this.botService = botService;
    }

    public abstract Command getCommand();

    public abstract void process(CommandPayload commandPayload);
}
