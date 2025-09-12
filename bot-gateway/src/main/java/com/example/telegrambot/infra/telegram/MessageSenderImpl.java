package com.example.telegrambot.infra.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@Slf4j
public class MessageSenderImpl implements MessageSender {

    private final TelegramClient telegramClient;

    public MessageSenderImpl(@Value("${telegram.bot.token}") String botToken,
                             TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @Override
    public void send(SendMessage message) {
        try {
            telegramClient.execute(message);
            log.info("Sent message to chat ID: {}", message.getChatId());
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat ID: {}. Error: {}",
                    message.getChatId(), e.getMessage());
        }
    }
}
