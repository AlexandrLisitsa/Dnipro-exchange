package exchange.service.rest;

import exchange.db.entity.Client;
import exchange.db.repository.UserRepo;
import exchange.telegram.BotService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
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
    public void notifyAll(@RequestBody NotifyRequest request) {
        List<Client> clients = userRepo.findAll();
        clients.forEach(client -> {
            botService.sendMessage(client.getChatId(), request.getMessage());
        });
    }

    @PostMapping("broadcast/notify/{number}")
    public ResponseEntity notifyByNumber(@RequestBody NotifyRequest message, @PathVariable String number) {
        Client client = userRepo.findClientByPhone(number);
        if (client == null) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        botService.sendMessage(client.getChatId(), message.getMessage());
        return new ResponseEntity(HttpStatus.OK);
    }

    @Data
    private static class NotifyRequest {
        private String message;
    }

}
