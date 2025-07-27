package com.example.telegrambot.service.registration;

import com.example.telegrambot.model.ConversationState;
import com.example.telegrambot.model.User;
import com.example.telegrambot.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Service
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {
    private final UserRepository userRepository;

    public RegistrationServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void registerUser(Message message) {
        if (message == null || message.getChat() == null) {
            log.warn("Received a message with no chat object. Cannot register user.");
            return;
        }

        var chatId = message.getChatId();
        if (userRepository.findById(chatId).isEmpty()) {
            var chat = message.getChat();
            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setConversationState(ConversationState.IDLE);

            userRepository.save(user);
            log.info("user was created for chatId: {}", chatId);
        }
    }
}
