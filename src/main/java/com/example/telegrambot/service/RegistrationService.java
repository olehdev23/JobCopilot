package com.example.telegrambot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public interface RegistrationService {
    void registerUser(Message message);
}
