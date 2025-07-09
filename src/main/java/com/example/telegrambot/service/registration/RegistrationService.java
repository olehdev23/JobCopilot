package com.example.telegrambot.service.registration;

import org.telegram.telegrambots.meta.api.objects.message.Message;

public interface RegistrationService {
    void registerUser(Message message);
}
