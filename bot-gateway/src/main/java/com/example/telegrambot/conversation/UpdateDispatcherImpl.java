package com.example.telegrambot.conversation;

import com.example.dto.AnalysisTaskDto;
import com.example.dto.ConversationState;
import com.example.dto.UserDto;
import com.example.telegrambot.infra.file.FileParserService;
import com.example.telegrambot.infra.kafka.KafkaProducerService;
import com.example.telegrambot.user.UserDataGateway;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Slf4j
@Service
public class UpdateDispatcherImpl implements UpdateDispatcher {

    private final ProfileService profileService;
    private final KafkaProducerService kafkaProducerService;
    private final FileParserService fileParserService;
    private final UserDataGateway userDataGateway;

    public UpdateDispatcherImpl(
            ProfileService profileService,
            KafkaProducerService kafkaProducerService,
            FileParserService fileParserService,
            UserDataGateway userDataGateway) {
        this.profileService = profileService;

        this.kafkaProducerService = kafkaProducerService;
        this.fileParserService = fileParserService;
        this.userDataGateway = userDataGateway;
    }

    @Override
    public Optional<SendMessage> dispatch(Message message) {
        Long chatId = message.getChatId();

        Optional<UserDto> userOptional = userDataGateway.getUser(chatId);

        if (userOptional.isPresent()) {
            return Optional.ofNullable(processUserState(userOptional.get(), message));

        } else {
            return Optional.of(new SendMessage(String.valueOf(chatId),
                "Welcome! Please type /start to begin."));
        }
    }

    private SendMessage processUserState(UserDto user, Message message) {
        ConversationState state = user.getConversationState();
        long chatId = user.getChatId();

        return switch (state) {
            case AWAITING_CV -> handleCvState(chatId, message);
            case AWAITING_PREFERENCES -> handlePreferencesState(chatId, message);
            case AWAITING_VACANCY -> handleVacancyState(user, message);
            case IDLE -> handleIdleState(user, message);
            default -> {
                log.warn("Reached an unexpected point in processUserState for state: {}", state);
                yield new SendMessage(String.valueOf(chatId),
                        "Sorry, an unexpected error occurred.");
            }
        };
    }

    private SendMessage handleCvState(long chatId, Message message) {
        if (message.hasDocument()) {
            try {
                String cvText = fileParserService.parse(message.getDocument());
                return profileService.handleCvText(chatId, cvText);
            } catch (Exception e) {
                log.error("Failed to parse CV for user {}", chatId, e);
                return new SendMessage(String.valueOf(chatId), "Failed to parse your CV file.");
            }
        }
        return new SendMessage(String.valueOf(chatId), "Please send your CV as a file.");
    }

    private SendMessage handlePreferencesState(long chatId, Message message) {
        if (message.hasText()) {
            return profileService.handlePreferences(chatId, message.getText());
        }
        return new SendMessage(String.valueOf(chatId),
                "Please provide your preferences as a text message.");
    }

    private SendMessage handleVacancyState(UserDto user, Message message) {
        if (message.hasText()) {
            String taskId = UUID.randomUUID().toString();
            Instant timestamp = Instant.now();
            kafkaProducerService.sendAnalysisTask(new AnalysisTaskDto(
                    taskId, user.getChatId(), timestamp, message.getText()));
            user.setConversationState(ConversationState.IDLE);
            userDataGateway.updateUser(user.getChatId(), user);
            return new SendMessage(String.valueOf(user.getChatId()),
                    "Received. Analyzing... 🤖");
        }
        return new SendMessage(String.valueOf(user.getChatId()),
                "Please send me your vacancy description");
    }

    private SendMessage handleIdleState(UserDto user, Message message) {
        if (message.hasText()) {
            String text = message.getText();
            if ("/prepare_application".equals(text)) {
                user.setConversationState(
                        ConversationState.AWAITING_VACANCY);
                userDataGateway.updateUser(user.getChatId(), user);
                return new SendMessage(String.valueOf(user.getChatId()),
                        "Alright, send me your vacancy description.");
            } else if ("Setup Profile".equals(text)) {
                return profileService
                        .startProfileSetup(user.getChatId());
            }
        }
        return new SendMessage(String.valueOf(user.getChatId()),
                "Unknown command. Please use the menu or available commands.");
    }
}

