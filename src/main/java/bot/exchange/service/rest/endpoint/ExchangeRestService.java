package bot.exchange.service.rest.endpoint;

import bot.commands.Command;
import bot.exchange.db.entity.Client;
import bot.exchange.db.repository.UserRepo;
import bot.exchange.telegram.BotService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
public class ExchangeRestService {

    @Autowired
    private BotService botService;
    @Autowired
    private UserRepo userRepo;

    @PostMapping("operation/updateOperationStatus")
    public ResponseEntity<HttpStatus> updateOperationStatus(@RequestBody UpdateOperationEntity updateOperation) {

        double rate = updateOperation.rate;
        String message = "До закінчення часу бронювання вашої операції з обміну валюти залишилося 15 хвилин.\n" +
                "Якщо не встигаєте, є змога продовжити операцію на 1 годину за курсом: " + rate;
        Client clientByPhone = userRepo.findClientByPhone(updateOperation.getPhone());
        String chatId = clientByPhone.getChatId();

        InlineKeyboardMarkup answerButtons = getAnswerButtons(updateOperation);
        botService.sendMessage(chatId, message, answerButtons);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private InlineKeyboardMarkup getAnswerButtons(UpdateOperationEntity operation) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        int code = operation.getCode();
        String phone = operation.getPhone();
        InlineKeyboardButton inTimeButton = getInTimeButton(code);

        InlineKeyboardButton prolongButton = getProlongButton(code, phone);

        InlineKeyboardButton rejectButton = getRejectButton(code, phone);

        List<List<InlineKeyboardButton>> buttons = Arrays.asList(
                Collections.singletonList(inTimeButton),
                Collections.singletonList(prolongButton),
                Collections.singletonList(rejectButton)
        );

        markup.setKeyboard(buttons);

        return markup;
    }

    private InlineKeyboardButton getInTimeButton(int code) {
        InlineKeyboardButton inTimeButton = new InlineKeyboardButton();
        inTimeButton.setText("Встигну.");
        inTimeButton.setCallbackData("/" + Command.BE_IN_TIME + ";" + code);

        return inTimeButton;
    }

    private InlineKeyboardButton getProlongButton(int code, String phone) {
        InlineKeyboardButton prolongRequest = new InlineKeyboardButton();
        prolongRequest.setText("Продовжити.");
        prolongRequest.setCallbackData("/" + Command.PROLONG_OPERATION + ";" + code + "," + phone);

        return prolongRequest;
    }

    private InlineKeyboardButton getRejectButton(int code, String phone) {
        InlineKeyboardButton prolongRequest = new InlineKeyboardButton();
        prolongRequest.setText("Відмовитись.");
        prolongRequest.setCallbackData("/" + Command.REJECT_OPERATION + ";" + code + "," + phone);

        return prolongRequest;
    }

    @Data
    private static class UpdateOperationEntity {
        private String phone;
        private int code;
        private double rate;
    }

}
