//package com.example.telegrambot.service.dispatcher;
//
//import com.example.dto.AnalysisTask;
//import com.example.dto.ConversationState;
//import com.example.model.User;
//import com.example.telegrambot.conversation.UpdateDispatcherImpl;
//import com.example.telegrambot.infra.telegram.MessageSender;
//
//import com.example.telegrambot.infra.file.FileParserService;
//import com.example.telegrambot.infra.kafka.KafkaProducerService;
//import com.example.telegrambot.conversation.ProfileService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.web.client.RestTemplate;
//import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
//import org.telegram.telegrambots.meta.api.objects.Document;
//import org.telegram.telegrambots.meta.api.objects.chat.Chat;
//import org.telegram.telegrambots.meta.api.objects.message.Message;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//
//import java.io.IOException;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class UpdateDispatcherImplTest {
//    @Mock
//    private ProfileService profileService;
//    @Mock
//    private RestTemplate restTemplate;
//    @Mock
//    private MessageSender messageSender;
//    @Mock
//    private KafkaProducerService kafkaProducerService;
//    @Mock
//    private FileParserService fileParserService;
//    @InjectMocks
//    private UpdateDispatcherImpl updateDispatcher;
//    @Captor
//    private ArgumentCaptor<AnalysisTask> taskCaptor;
//    private static final String USER_API_URL = "http://localhost:8081/api/v1/users/";
//    @Test
//    void whenIdleUserSendsPrepareCommand_thenStateShouldChangeToAwaitingVacancy() {
//        long chatId = 123L;
//        Message message = createMessage(chatId, "/prepare_application");
//        User user = createUser(chatId, ConversationState.IDLE);
//        when(restTemplate.getForObject(USER_API_URL + chatId, User.class)).thenReturn(user);
//
//        updateDispatcher.dispatch(message);
//
//        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
//        verify(restTemplate).put(eq(USER_API_URL + chatId), userCaptor.capture());
//
//        User savedUser = userCaptor.getValue();
//        assertEquals(ConversationState.AWAITING_VACANCY, savedUser.getConversationState());
//        verifyNoInteractions(kafkaProducerService, profileService);
//    }
//
//    @Test
//    void whenAwaitingVacancy_andReceivesText_thenSendsTaskToKafka() {
//        long chatId = 123L;
//        Message message  = createMessage(chatId, "My text to send as a task");
//        User user = createUser(chatId, ConversationState.AWAITING_VACANCY);
//        when(restTemplate.getForObject(USER_API_URL + chatId, User.class)).thenReturn(user);
//
//        updateDispatcher.dispatch(message);
//        verify(kafkaProducerService, times(1)).sendAnalysisTask(taskCaptor.capture());
//        assertEquals("My text to send as a task", taskCaptor.getValue().vacancyText());
//        verify(restTemplate).put(eq(USER_API_URL + chatId), any(User.class));
//        verifyNoInteractions(profileService, fileParserService);
//    }
//
//    @Test
//    void whenIdleUserSendsSetupProfile_thenProfileServiceIsCalled() {
//        long chatId = 123L;
//        Message message = createMessage(chatId, "Setup profile");
//
//        User user = createUser(chatId, ConversationState.IDLE);
//
//        when(restTemplate.getForObject(USER_API_URL + chatId, User.class)).thenReturn(user);
//
//
//        updateDispatcher.dispatch(message);
//
//        verify(profileService, times(1)).startProfileSetup(chatId);
//        verifyNoInteractions(kafkaProducerService);
//    }
//
//    //
//
//    @Test
//    void whenIdleUserSendsRandomText_thenReceivesUnknownCommandMessage() {
//        long chatId = 123L;
//        Message message = createMessage(chatId, "some random text");
//
//        User user = createUser(chatId, ConversationState.IDLE);
//
//        when(restTemplate.getForObject(USER_API_URL + chatId, User.class)).thenReturn(user);
//
//        updateDispatcher.dispatch(message);
//
//        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
//        verify(messageSender, times(1)).send(messageCaptor.capture());
//        verify(restTemplate, times(1)).getForObject(USER_API_URL + chatId, User.class);
//        verifyNoInteractions(kafkaProducerService, profileService);
//        SendMessage receivedMessage = messageCaptor.getValue();
//        assertEquals("Unknown command. Please use the menu or available commands.", receivedMessage.getText());
//    }
//
//    @Test
//    void whenAwaitingVacancy_andReceivesNonTextMessage_thenAsksForText() {
//        long chatId = 123L;
//        User user = createUser(chatId, ConversationState.AWAITING_VACANCY);
//
//        Message message = new Message();
//
//        when(restTemplate.getForObject(USER_API_URL + chatId, User.class)).thenReturn(user);
//
//        updateDispatcher.dispatch(message);
//
//        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
//        verify(messageSender, times(1)).send(messageCaptor.capture());
//        verify(restTemplate, times(1)).getForObject(USER_API_URL + chatId, User.class);
//        verifyNoMoreInteractions(restTemplate);
//        verifyNoInteractions(kafkaProducerService, profileService);
//        SendMessage receivedMessage = messageCaptor.getValue();
//        assertEquals("Please send me your vacancy description", receivedMessage.getText());
//    }
//
//    //
//    @Test
//    void whenAwaitingCv_andReceivesMessage_thenDelegatesToProfileService() throws TelegramApiException, IOException {
//        long chatId = 123L;
//        User user = createUser(chatId, ConversationState.AWAITING_CV);
//        Message message = createMessageWithDocument(chatId);
//        String parsedText = "parsed cv text";
//        SendMessage expectedResponse = new SendMessage(String.valueOf(chatId), "Your CV has been processed.");
//
//        when(restTemplate.getForObject(USER_API_URL + chatId, User.class)).thenReturn(user);
//        when(fileParserService.parse(any(Document.class))).thenReturn(parsedText);
//        // Створюємо будь-який об'єкт SendMessage, щоб просто повернути щось, що не є null
//
//        when(profileService.handleCvText(chatId, parsedText)).thenReturn(expectedResponse);
//
//        updateDispatcher.dispatch(message);
//
//        verify(restTemplate, times(1)).getForObject(USER_API_URL + chatId, User.class);
//        verify(profileService, times(1)).handleCvText(chatId, parsedText);
//        verify(fileParserService).parse(any(Document.class));
//        verify(messageSender).send(expectedResponse);
//        verifyNoMoreInteractions(fileParserService, profileService, messageSender);
//        verifyNoInteractions(kafkaProducerService);
//    }
//
//    //
//    @Test
//    void whenUnregisteredUserSendsMessage_thenReceivesWelcomePrompt() {
//        long chatId = 123L;
//        Message message = createMessage(chatId, "some message");
//
//        when(restTemplate.getForObject(USER_API_URL + chatId, User.class))
//                .thenThrow(new org.springframework.web.client.HttpClientErrorException(
//                        org.springframework.http.HttpStatus.NOT_FOUND));
//
//        updateDispatcher.dispatch(message);
//
//        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
//
//        verify(restTemplate, times(1)).getForObject(USER_API_URL + chatId, User.class);
//        verify(messageSender, times(1)).send(messageCaptor.capture());
//        assertEquals("Welcome! Please type /start to begin.", messageCaptor.getValue().getText());
//        verifyNoMoreInteractions(profileService, kafkaProducerService, restTemplate);
//    }
//
//    @Test
//    void dispatch_whenAwaitingPreferences_andReceivesText_delegatesToProfileService() {
//        // Arrange
//        long chatId = 123L;
//        User user = createUser(chatId, ConversationState.AWAITING_PREFERENCES);
//        String preferences = "I want to work with Java.";
//        Message message = createMessage(chatId, preferences);
//        when(restTemplate.getForObject(USER_API_URL + chatId, User.class)).thenReturn(user);
//
//        // Act
//        updateDispatcher.dispatch(message);
//
//        // Assert
//        verify(profileService).handlePreferences(chatId, preferences);
//        verifyNoInteractions(kafkaProducerService, fileParserService);
//    }
//
//    @Test
//    void whenGetUserFailsWithServerError_thenSendsErrorMessage() {
//        long chatId = 123L;
//        Message message = createMessage(chatId, "any message");
//        // Мокуємо викидання іншого винятку
//        when(restTemplate.getForObject(anyString(), eq(User.class)))
//                .thenThrow(new org.springframework.web.client.HttpServerErrorException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR));
//
//        updateDispatcher.dispatch(message);
//
//        // Очікуємо, що користувачу відправиться повідомлення про помилку
//        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
//        verify(messageSender).send(messageCaptor.capture());
//        assertEquals("Welcome! Please type /start to begin.", messageCaptor.getValue().getText());        // Тут має бути повідомлення, яке ми відправляємо, коли не можемо отримати юзера
//        // Наразі твій код відправить "Welcome", бо getUser поверне empty().
//        // Це теж важливе спостереження з тесту! Можливо, варто розрізняти "новий юзер" і "сервіс недоступний"?
//    }
//
//    @Test
//    void whenAwaitingCv_andFileParsingFails_thenSendsErrorMessage() throws IOException, TelegramApiException {
//        long chatId = 123L;
//        User user = createUser(chatId, ConversationState.AWAITING_CV);
//        Message message = createMessageWithDocument(chatId);
//        when(restTemplate.getForObject(anyString(), eq(User.class))).thenReturn(user);
//
//        when(fileParserService.parse(any(Document.class))).thenThrow(new IOException("Test parsing error"));
//
//        updateDispatcher.dispatch(message);
//
//        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
//        verify(messageSender).send(captor.capture());
//        assertEquals("Failed to parse your CV file.", captor.getValue().getText());
//        verifyNoInteractions(profileService, kafkaProducerService);
//    }
//
//    private User createUser(long chatId, ConversationState state) {
//        User user = new User();
//        user.setChatId(chatId);
//        user.setConversationState(state);
//        return user;
//    }
//
//    private Message createMessage(long chatId, String text) {
//        Message message = new Message();
//        message.setChat(new Chat(chatId, "private"));
//        message.setText(text);
//        return message;
//    }
//
//    private Message createMessageWithDocument(long chatId) {
//        Message message = new Message();
//        message.setChat(new Chat(chatId, "private"));
//        message.setDocument(new Document());
//        return message;
//    }
//}