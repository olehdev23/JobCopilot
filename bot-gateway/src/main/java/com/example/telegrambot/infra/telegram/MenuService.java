package com.example.telegrambot.infra.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MessageSender messageSender;

    public void sendMainMenu(long chatId, String userName) {
        String responseText = "Hello, " + userName
                + "! I've registered you.\n\n"
                + "Let's set up your profile so I can provide the best assistance.";

        SendMessage message = new SendMessage(String.valueOf(chatId), responseText);

        ReplyKeyboardMarkup keyboardMarkup = ReplyKeyboardMarkup.builder().keyboardRow(
                        new KeyboardRow("Setup Profile"))
                .resizeKeyboard(true).oneTimeKeyboard(true).build();

        message.setReplyMarkup(keyboardMarkup);

        messageSender.send(message);
    }
}
