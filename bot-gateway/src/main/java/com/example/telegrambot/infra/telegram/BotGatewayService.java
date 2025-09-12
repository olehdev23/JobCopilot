package com.example.telegrambot.infra.telegram;

import com.example.dto.ConversationState;
import com.example.dto.UserDto;
import com.example.telegrambot.conversation.UpdateDispatcher;
import com.example.telegrambot.user.UserDataGateway;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Service
@Slf4j
@RequiredArgsConstructor
public class BotGatewayService {

    private final UserDataGateway userDataGateway;
    private final MenuService menuService;
    private final UpdateDispatcher updateDispatcher;
    private final MessageSender messageSender;

    public void processMessage(Message message) {
        String messageText = message.getText();
        long chatId = message.getChatId();
        boolean userExists = userDataGateway.getUser(chatId).isPresent();
        if ("/start".equals(messageText) && !userExists) {
            UserDto newUserDto = new UserDto();
            newUserDto.setChatId(chatId);
            newUserDto.setFirstName(message.getChat().getFirstName());
            newUserDto.setLastName(message.getChat().getLastName());
            newUserDto.setUserName(message.getChat().getUserName());
            newUserDto.setConversationState(ConversationState.IDLE);
            userDataGateway.registerNewUser(newUserDto);
            menuService.sendMainMenu(chatId, message.getChat().getFirstName());
        } else {
            Optional<SendMessage> response = updateDispatcher.dispatch(message);
            response.ifPresent(messageSender::send);
        }
    }
}
