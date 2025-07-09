package com.example.telegrambot.service.dispatcher;

import com.example.telegrambot.bot.MessageSender;
import com.example.telegrambot.dto.AnalysisTask;
import com.example.telegrambot.model.ConversationState;
import com.example.telegrambot.model.User;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.kafka.KafkaProducerService;
import com.example.telegrambot.service.profile.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Slf4j
@Service
public class UpdateDispatcherImpl implements UpdateDispatcher {

    private final ProfileService profileService;
    private final UserRepository userRepository;
    private final MessageSender messageSender;
    private final KafkaProducerService kafkaProducerService;

    public UpdateDispatcherImpl(
            ProfileService profileService,
            UserRepository userRepository, @Lazy MessageSender messageSender,
            KafkaProducerService kafkaProducerService) {
        this.profileService = profileService;
        this.userRepository = userRepository;
        this.messageSender = messageSender;
        this.kafkaProducerService = kafkaProducerService;
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
                    AnalysisTask task = new AnalysisTask(chatId, vacancyText);
                    kafkaProducerService.sendAnalysisTask(task);

                    user.setConversationState(ConversationState.IDLE);
                    userRepository.save(user);

                    return new SendMessage(String.valueOf(chatId), "Received. Analyzing... 🤖");
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
}