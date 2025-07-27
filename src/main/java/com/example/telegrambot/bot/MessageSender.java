package com.example.telegrambot.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface MessageSender {
    void send(SendMessage message);
}
