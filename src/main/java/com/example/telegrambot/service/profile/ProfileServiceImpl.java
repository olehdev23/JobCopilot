package com.example.telegrambot.service.profile;

import com.example.telegrambot.model.ConversationState;
import com.example.telegrambot.model.User;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.file.FileParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Slf4j
@Service
public class ProfileServiceImpl implements ProfileService {
    private final UserRepository userRepository;
    private final FileParserService fileParserService;

    public ProfileServiceImpl(UserRepository userRepository, FileParserService fileParserService) {
        this.userRepository = userRepository;
        this.fileParserService = fileParserService;
    }

    @Override
    public SendMessage startProfileSetup(User user) {
        user.setConversationState(ConversationState.AWAITING_CV);

        userRepository.save(user);
        log.info("User {} state changed to {}", user.getChatId(), user.getConversationState());

        String responseText = "Great! Let's start with your resume."
                + "\n\nPlease send me your CV as a file (.pdf or .docx).";

        return new SendMessage(String.valueOf(user.getChatId()), responseText);
    }

    @Override
    public SendMessage processMessage(User user, Message message) {
        ConversationState state = user.getConversationState();
        long chatId = user.getChatId();

        return switch (state) {
            case AWAITING_CV -> handleCvUpload(user, message);
            case AWAITING_PREFERENCES -> handlePreferencesInput(user, message);
            default -> {
                log.warn(
                        "processMessage called in an unexpected state: {} for user {}",
                        state, chatId);
                yield new SendMessage(String.valueOf(chatId),
                        "Sorry, something went wrong. Let's start over by pressing /start.");
            }
        };
    }

    private SendMessage handleCvUpload(User user, Message message) {
        long chatId = user.getChatId();

        if (!message.hasDocument()) {
            return new SendMessage(String.valueOf(chatId),
                    "This step requires a file. Please send your CV as a .pdf or .docx file.");
        }

        Document cvDocument = message.getDocument();
        log.info("Received CV file '{}' from user {}", cvDocument.getFileName(), chatId);

        try {
            String parsedCvText = fileParserService.parse(cvDocument);
            user.setCv(parsedCvText);

            user.setConversationState(ConversationState.AWAITING_PREFERENCES);
            userRepository.save(user);
            log.info("User {} state changed to {}", chatId, user.getConversationState());

            String responseText = "Thank you, I've successfully parsed and saved your CV.\n\n"
                    + "Now, please describe your job preferences in a single message.";
            return new SendMessage(String.valueOf(chatId), responseText);

        } catch (Exception e) {
            log.error("Failed to parse file for user {}",
                    chatId, e);
            return new SendMessage(String.valueOf(chatId),
                    "Sorry, I failed to process your file."
                            + " Please try another one or contact support.");
        }
    }

    private SendMessage handlePreferencesInput(User user, Message message) {
        if (!message.hasText()) {
            return new SendMessage(String.valueOf(user.getChatId()),
                    "Please provide your preferences as a text message.");
        }

        log.info("Handling AWAITING_PREFERENCES state for user {}", user.getChatId());

        user.setConversationState(ConversationState.IDLE);
        String text = message.getText();
        user.setPreferences(text);
        userRepository.save(user);

        log.info("User {} state changed to IDLE. Profile setup finished.",
                user.getChatId());
        String responseText = "Excellent! Your profile is now complete. \n\n"
                + "You can now use the /prepare_application command. "
                + "Send it to me, and I will prompt you for a job vacancy. "
                + "Then, I will generate a tailored cover letter and provide "
                + "you with interview preparation tips based on your profile.";
        return new SendMessage(String.valueOf(user.getChatId()), responseText);
    }
}
