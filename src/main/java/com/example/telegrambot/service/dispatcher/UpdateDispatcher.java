package com.example.telegrambot.service.dispatcher;

import org.telegram.telegrambots.meta.api.objects.message.Message;

public interface UpdateDispatcher {
    void dispatch(Message message);
}