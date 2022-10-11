package exchange.statemachine;

import lombok.Data;

@Data
public class Payload {
    private long chatId;
    private String userId;
    private Event event;
    private String context;
}
