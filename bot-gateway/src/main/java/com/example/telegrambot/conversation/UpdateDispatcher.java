package com.example.telegrambot.conversation;

import java.util.Optional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public interface UpdateDispatcher {
    Optional<SendMessage> dispatch(Message message);
}
