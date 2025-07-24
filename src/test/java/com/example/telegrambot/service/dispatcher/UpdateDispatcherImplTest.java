package com.example.telegrambot.service.dispatcher;

import com.example.telegrambot.bot.MessageSender;
import com.example.telegrambot.dto.AnalysisTask;
import com.example.telegrambot.model.ConversationState;
import com.example.telegrambot.model.User;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.kafka.KafkaProducerService;
import com.example.telegrambot.service.profile.ProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateDispatcherImplTest {
    @Mock
    private ProfileService profileService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MessageSender messageSender;
    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private UpdateDispatcherImpl updateDispatcher;


    @Test
    void whenIdleUserSendsPrepareCommand_thenStateShouldChangeToAwaitingVacancy() {
        Long chatId = 123L;
        Chat chat = new Chat(chatId, "private");

        Message message = new Message();
        message.setChat(chat);
        message.setText("/prepare_application");

        User user = new User();
        user.setChatId(chatId);
        user.setConversationState(ConversationState.IDLE);

        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));

        updateDispatcher.dispatch(message);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        verify(userRepository, times(1)).findById(chatId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(kafkaProducerService, profileService);
        User savedUser = userCaptor.getValue();
        assertEquals(ConversationState.AWAITING_VACANCY, savedUser.getConversationState());
    }

    @Test
    void whenAwaitingVacancy_andReceivesText_thenSendsTaskToKafka() {
        Long chatId = 123L;
        Chat chat = new Chat(chatId, "private");

        Message message = new Message();
        message.setChat(chat);
        message.setText("My text to send as a task");

        User user = new User();
        user.setChatId(chatId);
        user.setConversationState(ConversationState.AWAITING_VACANCY);

        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));

        updateDispatcher.dispatch(message);

        ArgumentCaptor<AnalysisTask> taskCaptor = ArgumentCaptor.forClass(AnalysisTask.class);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(kafkaProducerService, times(1)).sendAnalysisTask(taskCaptor.capture());
        verify(userRepository, times(1)).save(userCaptor.capture());
        verifyNoInteractions(profileService);

        AnalysisTask sentTask = taskCaptor.getValue();
        assertEquals("My text to send as a task", sentTask.vacancyText());

        User savedUser = userCaptor.getValue();
        assertEquals(ConversationState.IDLE, savedUser.getConversationState());
    }

    @Test
    void whenIdleUserSendsSetupProfile_thenProfileServiceIsCalled() {
        Long chatId = 123L;
        Chat chat = new Chat(chatId, "private");

        Message message = new Message();
        message.setChat(chat);
        message.setText("Setup Profile");

        User user = new User();
        user.setChatId(chatId);
        user.setConversationState(ConversationState.IDLE);

        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));

        updateDispatcher.dispatch(message);

        verify(profileService, times(1)).startProfileSetup(user);
        verifyNoMoreInteractions(profileService, userRepository);
        verifyNoInteractions(kafkaProducerService);
    }

    @Test
    void whenIdleUserSendsRandomText_thenReceivesUnknownCommandMessage() {
        Long chatId = 123L;
        Chat chat = new Chat(chatId, "private");

        Message message = new Message();
        message.setChat(chat);
        message.setText("Hello world!");

        User user = new User();
        user.setChatId(chatId);
        user.setConversationState(ConversationState.IDLE);

        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));

        updateDispatcher.dispatch(message);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(messageSender, times(1)).send(messageCaptor.capture());
        verify(userRepository, times(1)).findById(chatId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(kafkaProducerService, profileService);
        SendMessage receivedMessage = messageCaptor.getValue();
        assertEquals("Unknown command. Please use the menu or available commands.", receivedMessage.getText());
    }

    @Test
    void whenAwaitingVacancy_andReceivesNonTextMessage_thenAsksForText() {
        Long chatId = 123L;
        Chat chat = new Chat(chatId, "private");

        User user = new User();
        user.setChatId(chatId);
        user.setConversationState(ConversationState.AWAITING_VACANCY);

        Message message = new Message();
        message.setChat(chat);

        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));

        updateDispatcher.dispatch(message);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(messageSender, times(1)).send(messageCaptor.capture());
        verify(userRepository, times(1)).findById(chatId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(kafkaProducerService, profileService);
        SendMessage receivedMessage = messageCaptor.getValue();
        assertEquals("Please send me your vacancy description", receivedMessage.getText());
    }

    @Test
    void whenAwaitingCv_andReceivesMessage_thenDelegatesToProfileService() {
        Long chatId = 123L;
        Chat chat = new Chat(chatId, "private");

        User user = new User();
        user.setChatId(chatId);
        user.setConversationState(ConversationState.AWAITING_CV);

        Message message = new Message();
        message.setChat(chat);

        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));

        updateDispatcher.dispatch(message);

        verify(userRepository, times(1)).findById(chatId);
        verify(profileService, times(1)).processMessage(user, message);
        verifyNoMoreInteractions(userRepository, profileService);
        verifyNoInteractions(kafkaProducerService);
    }

    @Test
    void whenUnregisteredUserSendsMessage_thenReceivesWelcomePrompt() {
        Long chatId = 123L;
        Chat chat = new Chat(chatId, "private");

        Message message = new Message();
        message.setChat(chat);
        message.setText("hello");

        when(userRepository.findById(chatId)).thenReturn(Optional.empty());

        updateDispatcher.dispatch(message);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);

        verify(userRepository, times(1)).findById(chatId);
        verify(messageSender, times(1)).send(messageCaptor.capture());
        assertEquals("Welcome! Please type /start to begin.", messageCaptor.getValue().getText());
        verifyNoMoreInteractions(profileService, kafkaProducerService, userRepository);
        verifyNoInteractions(profileService, kafkaProducerService);
    }
}