package com.example.telegrambot.service;

import com.example.telegrambot.model.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public interface ProfileService {

    SendMessage startProfileSetup(User user);

    SendMessage processMessage(User user, Message message);
}