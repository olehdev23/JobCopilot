package com.example.telegrambot.service.impl;

import com.example.telegrambot.bot.MessageSender;
import com.example.telegrambot.model.ConversationState;
import com.example.telegrambot.model.User;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.ProfileService;
import com.example.telegrambot.service.UpdateDispatcher;
import com.example.telegrambot.service.VacancyAnalysisService;
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
    private final VacancyAnalysisService vacancyAnalysisService;

    public UpdateDispatcherImpl(
            ProfileService profileService,
            UserRepository userRepository, @Lazy MessageSender messageSender, VacancyAnalysisService vacancyAnalysisService) {
        this.profileService = profileService;
        this.userRepository = userRepository;
        this.messageSender = messageSender;
        this.vacancyAnalysisService = vacancyAnalysisService;
    }

    @Override
    public void dispatch(Message message) {
        Long chatId = message.getChatId();
        userRepository.findById(chatId).ifPresentOrElse(user -> {
            SendMessage response = processUserState(user, message);
            if (response != null) {
                messageSender.send(response);
            }
        }, () -> {
            // Обробка незареєстрованого користувача
            messageSender.send(new SendMessage(String.valueOf(chatId), "Welcome! Please type /start to begin."));
        });
    }

    private SendMessage processUserState(User user, Message message) {
        ConversationState state = user.getConversationState();
        long chatId = user.getChatId();

        switch (state) {
            case AWAITING_CV, AWAITING_PREFERENCES:
                return profileService.processMessage(user, message);

            case AWAITING_VACANCY:
                if (message.hasText()) {
                    String vacancyText = message.getText();
                    messageSender.send(new SendMessage(String.valueOf(chatId), "Received. Analyzing... 🤖"));

                    String analysisResult = vacancyAnalysisService.analyze(user, vacancyText);

                    user.setConversationState(ConversationState.IDLE);
                    userRepository.save(user);

                    return new SendMessage(String.valueOf(chatId), analysisResult);
                } else {
                    return new SendMessage(String.valueOf(chatId), "Please send me your vacancy description");
                }

            case IDLE:
                if (message.hasText()) {
                    String text = message.getText();
                    if ("/prepare_application".equals(text)) {
                        user.setConversationState(ConversationState.AWAITING_VACANCY);
                        userRepository.save(user);
                        return new SendMessage(String.valueOf(chatId), "Alright, send me your vacancy description.");
                    } else if ("Setup Profile".equals(text)) {
                        return profileService.startProfileSetup(user);
                    }
                }
                return new SendMessage(String.valueOf(chatId), "Unknown command. Please use the menu or available commands.");
        }
        log.warn("Reached an unexpected point in processUserState for state: {}", state);
        return new SendMessage(String.valueOf(chatId), "Sorry, an unexpected error occurred.");
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