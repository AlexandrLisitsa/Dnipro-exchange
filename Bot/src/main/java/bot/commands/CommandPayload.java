package bot.commands;

import lombok.Data;

@Data
public class CommandPayload {
    private String chatId;
    private String data;
    private Command command;
}
