package bot.exchange.statemachine;

import lombok.Data;

@Data
public class Payload {
    private String chatId;
    private int messageId;
    private String userId;
    private Event event;
    private String context;
    private boolean isCallback;
}
