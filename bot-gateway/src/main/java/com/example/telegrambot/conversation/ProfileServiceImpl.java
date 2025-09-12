package com.example.telegrambot.conversation;

import com.example.dto.ConversationState;
import com.example.dto.UserDto;
import com.example.telegrambot.exception.ProfileUpdateException;
import com.example.telegrambot.exception.UserNotFoundException;
import com.example.telegrambot.user.UserDataGateway;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Slf4j
@Service
public class ProfileServiceImpl implements ProfileService {
    /**
     * Gateway for accessing user data from the user-data-access microservice.
     */
    private final UserDataGateway userDataGateway;

    /**
     * Constructs a ProfileServiceImpl with the necessary dependencies.
     *
     * @param userDataGateway The gateway for user data access.
     */
    public ProfileServiceImpl(UserDataGateway userDataGateway) {
        this.userDataGateway = userDataGateway;
    }

    @Override
    public SendMessage startProfileSetup(long chatId) {
        String messageText;
        try {
            updateUserProfile(chatId, user -> {
                user.setConversationState(ConversationState.AWAITING_CV);
            });

            messageText = "Great! Let's start with your resume."
                    + "\n\nPlease send me your CV as a file (.pdf or .docx).";
        } catch (UserNotFoundException | ProfileUpdateException e) {
            log.error("Failed to setup profile for chatId {}: {}", chatId, e.getMessage());
            messageText = "Sorry, a temporary service error occurred. Please try again later.";
        }
        return new SendMessage(String.valueOf(chatId), messageText);
    }

    @Override
    public SendMessage handleCvText(long chatId, String cvText) {
        String messageText;
        try {
            updateUserProfile(chatId, user -> {
                user.setCv(cvText);
                user.setConversationState(ConversationState.AWAITING_PREFERENCES);
            });
            messageText = "Thank you, I've successfully parsed and saved your CV.\n\n"
                    + "Now, please describe your job preferences in a single message.";
        } catch (UserNotFoundException | ProfileUpdateException e) {
            log.error("Failed to set cv text for chatId {}: {}", chatId, e.getMessage());
            messageText = "Sorry, a temporary service error occurred. Please try again later.";
        }
        return new SendMessage(String.valueOf(chatId), messageText);
    }

    @Override
    public SendMessage handlePreferences(long chatId, String preferencesText) {
        String messageText;
        try {
            updateUserProfile(chatId, user -> {
                user.setPreferences(preferencesText);
                user.setConversationState(ConversationState.IDLE);
            });
            messageText = "Excellent! Your profile is now complete. \n\n"
                    + "You can now use the /prepare_application command...";
        } catch (UserNotFoundException | ProfileUpdateException e) {
            log.error("Failed to set preferences text for chatId {}: {}", chatId, e.getMessage());
            messageText = "Sorry, a temporary service error occurred. Please try again later.";
        }
        return new SendMessage(String.valueOf(chatId), messageText);
    }

    private void updateUserProfile(long chatId, Consumer<UserDto> userModifier) {
        userDataGateway.getUser(chatId)
                .ifPresentOrElse(
                        user -> {
                            userModifier.accept(user);
                            userDataGateway.updateUser(chatId, user);
                        },
                        () -> {
                            throw new UserNotFoundException("User not found for chatId: " + chatId);
                        });
    }
}
