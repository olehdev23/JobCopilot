package com.example.telegrambot.service.impl;

import com.example.telegrambot.bot.MessageSender;
import com.example.telegrambot.model.ConversationState;
import com.example.telegrambot.model.User;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.ProfileService;
import com.example.telegrambot.service.UpdateDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

@Slf4j
@Service
public class UpdateDispatcherImpl implements UpdateDispatcher {

    private final ProfileService profileService;
    private final UserRepository userRepository;
    private final MessageSender messageSender;

    public UpdateDispatcherImpl(
                                ProfileService profileService,
                                UserRepository userRepository, @Lazy MessageSender messageSender) {
        this.profileService = profileService;
        this.userRepository = userRepository;
        this.messageSender = messageSender;
    }

    @Override
    public void dispatch(Message message) {
        long chatId = message.getChatId();

        Optional<User> userOptional = userRepository.findById(chatId);

        if (userOptional.isEmpty()) {
            SendMessage response = new SendMessage(String.valueOf(chatId), "Welcome! Please type /start to begin.");
            messageSender.send(response);
            return;
        }

        User user = userOptional.get();
        ConversationState state = user.getConversationState();

        SendMessage response = null;

        if (state != ConversationState.IDLE) {
            response = profileService.processMessage(user, message);
        } else if (message.hasText()) {
            String text = message.getText();
            if ("Setup Profile".equals(text)) {
                response = profileService.startProfileSetup(user);
            } else {
                response = new SendMessage(String.valueOf(chatId), "Command not recognized.");
            }
        } else {
            response = new SendMessage(String.valueOf(chatId), "I don't understand this format. Please use text commands or buttons.");
        }
        if (response != null) {
            messageSender.send(response);
        }
    }

    /**
     * A simple helper method to send a plain text message.
     * @param chatId The ID of the chat to send the message to.
     * @param text   The text of the message.
     */
    private void sendTextMessage(long chatId, String text) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        messageSender.send(message);
    }
}