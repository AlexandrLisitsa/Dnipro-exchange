package exchange.service.rest;

import exchange.db.entity.Client;
import exchange.db.repository.UserRepo;
import exchange.telegram.BotService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BroadcastService {

    @Autowired
    private BotService botService;
    @Autowired
    private UserRepo userRepo;

    @PostMapping("broadcast/notifyAll")
    public void notifyAll(@RequestBody NotifyAllRequest request) {
        List<Client> clients = userRepo.findAll();
        clients.forEach(client -> {
            botService.sendMessage(client.getChatId(), request.getMessage());
        });
    }

    @Data
    private static class NotifyAllRequest {
        private String message;
    }

}
