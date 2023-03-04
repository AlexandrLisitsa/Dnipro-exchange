package bot.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommandHandler {

    @Autowired
    private List<BaseCommand> commands;

    public void handleCommand(CommandPayload payload) {
        commands.stream()
                .filter(baseCommand -> baseCommand.getCommand().equals(payload.getCommand()))
                .findFirst()
                .ifPresent(baseCommand -> baseCommand.process(payload));
    }

}
