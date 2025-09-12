//package com.example.telegrambot.service.profile;
//
//import com.example.dto.ConversationState;
//import com.example.model.User;
//import com.example.telegrambot.conversation.ProfileServiceImpl;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.MethodSource;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.web.client.RestClientException;
//import org.springframework.web.client.RestTemplate;
//import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
//
//import java.util.function.Function;
//import java.util.stream.Stream;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ProfileServiceImplTest {
//
//    @Mock
//    private RestTemplate restTemplate;
//    @InjectMocks
//    private ProfileServiceImpl profileService;
//    @Captor
//    private ArgumentCaptor<User> userCaptor;
//    private static final String USER_API_URL = "http://localhost:8081/api/users/";
//
//    @Test
//    void startProfileSetup_whenUserExist_updatesStateAndReturnsSuccessMessage() {
//        // Arrange
//        long chatId = 123L;
//        User userFromServer = new User();
//        userFromServer.setChatId(chatId);
//        userFromServer.setConversationState(ConversationState.IDLE);
//
//        // Імітуємо, що GET-запит повертає юзера
//        when(restTemplate.getForObject(USER_API_URL + chatId, User.class)).thenReturn(userFromServer);
//
//        // Act
//        SendMessage response = profileService.startProfileSetup(chatId);
//
//        // Assert
//        // Перевіряємо, що ми відправили оновленого юзера через PUT-запит
//        verify(restTemplate).put(eq(USER_API_URL + chatId), userCaptor.capture());
//
//        User updatedUser = userCaptor.getValue();
//        assertEquals(ConversationState.AWAITING_CV, updatedUser.getConversationState());
//
//        // Перевіряємо текст відповіді
//        assertTrue(response.getText().contains("Let's start with your resume"));
//    }
//
//    @Test
//    void handleCvText_whenItsCorrect_updateUserWithCvAndChangeState() {
//        // Arrange
//        long chatId = 456L;
//        String cvText = "Parsed CV text";
//        User userFromServer = new User();
//        userFromServer.setChatId(chatId);
//        userFromServer.setConversationState(ConversationState.AWAITING_CV);
//
//        when(restTemplate.getForObject(USER_API_URL + chatId, User.class)).thenReturn(userFromServer);
//
//        // Act
//        SendMessage response = profileService.handleCvText(chatId, cvText);
//
//        // Assert
//        verify(restTemplate).put(eq(USER_API_URL + chatId), userCaptor.capture());
//
//        User updatedUser = userCaptor.getValue();
//        assertEquals(cvText, updatedUser.getCv());
//        assertEquals(ConversationState.AWAITING_PREFERENCES, updatedUser.getConversationState());
//        assertTrue(response.getText().contains("Thank you, I've successfully parsed and saved your CV." +
//                "\n\nNow, please describe your job preferences in a single message."));
//
//    }
//
//    @Test
//    void handlePreferences_whenItsCorrect_UpdateUserWithPrefsAndChangeState() {
//        // Arrange
//        long chatId = 789L;
//        String preferencesText = "Java, Spring, Remote";
//        User userFromServer = new User();
//        userFromServer.setChatId(chatId);
//        userFromServer.setConversationState(ConversationState.AWAITING_PREFERENCES);
//
//        when(restTemplate.getForObject(USER_API_URL + chatId, User.class)).thenReturn(userFromServer);
//
//        // Act
//        SendMessage response = profileService.handlePreferences(chatId, preferencesText);
//
//        // Assert
//        verify(restTemplate).put(eq(USER_API_URL + chatId), userCaptor.capture());
//
//        User updatedUser = userCaptor.getValue();
//        assertEquals(preferencesText, updatedUser.getPreferences());
//        assertEquals(ConversationState.IDLE, updatedUser.getConversationState());
//        assertTrue(response.getText().contains("Excellent! Your profile is now complete. \n\nYou can now use the /prepare_application command..."));
//    }
//
//    @ParameterizedTest
//    @MethodSource("profileUpdateMethodsProvider")
//    void publicMethods_whenUserNotFound_shouldReturnErrorMessage(
//            Function<ProfileServiceImpl, SendMessage> methodCaller) {
//
//        // Arrange
//        long chatId = 123L;
//        // Імітуємо, що при спробі отримати юзера виникає помилка (наприклад, сервіс недоступний)
//        // Ми мокуємо виклик, який робить наш приватний метод updateUserProfile
//        when(restTemplate.getForObject(USER_API_URL + chatId, User.class))
//                .thenReturn(null);
//
//        // Act
//        SendMessage response = methodCaller.apply(profileService);
//
//        // Assert
//        // 1. Переконуємось, що ми навіть не намагались оновити юзера, бо впали раніше
//        verify(restTemplate, never()).put(anyString(), any(User.class));
//
//        // 2. Перевіряємо, що користувач отримав коректне повідомлення про помилку
//        assertTrue(response.getText().contains("Sorry, a temporary service error occurred."));
//    }
//
//    @ParameterizedTest
//    @MethodSource("profileUpdateMethodsProvider")
//    void publicMethods_whenUpdateFails_returnsErrorAndDoesNotUpdate(
//            Function<ProfileServiceImpl, SendMessage> methodCaller) {
//        // Arrange
//        // Імітуємо один тип збою, наприклад, коли сервіс недоступний
//        when(restTemplate.getForObject(anyString(), any())).thenThrow(new RestClientException("Service down"));
//
//        // Act
//        // Викликаємо метод, який прийшов як параметр, і отримуємо результат
//        SendMessage response = methodCaller.apply(profileService);
//
//        // Assert
//        // Перевіряємо, що користувач отримав правильне повідомлення про помилку
//        assertTrue(response.getText().contains("Sorry, a temporary service error occurred."));
//        // Переконуємось, що ми не намагались нічого оновити
//        verify(restTemplate, never()).put(anyString(), any(User.class));
//    }
//
//    // Оновлюємо постачальника, щоб він повертав Function
//    static Stream<Function<ProfileServiceImpl, SendMessage>> profileUpdateMethodsProvider() {
//        return Stream.of(
//                service -> service.startProfileSetup(123L),
//                service -> service.handleCvText(123L, "some cv"),
//                service -> service.handlePreferences(123L, "some prefs")
//        );
//    }
//}