//package com.example.telegrambot.service.registration;
//
//import com.example.telegrambot.integration.BaseIntegrationTest;
//import com.example.telegrambot.model.ConversationState;
//import com.example.telegrambot.model.User;
//import com.example.telegrambot.repository.UserRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.jdbc.Sql;
//import org.telegram.telegrambots.meta.api.objects.chat.Chat;
//import org.telegram.telegrambots.meta.api.objects.message.Message;
//
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//@Sql(scripts = "/sql/clean-up.sql",
//        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
//class RegistrationServiceImplTest extends BaseIntegrationTest {
//
//    @Autowired
//    private RegistrationServiceImpl registrationService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Test
//    void whenRegisterUser_thenUserIsSavedToDatabase() {
//        long chatId = 1234L;
//        String firstName = "John";
//        String lastName = "Doe";
//        String userName = "johndoe";
//
//        Chat testChat = new Chat(chatId, "private");
//        testChat.setFirstName(firstName);
//        testChat.setLastName(lastName);
//        testChat.setUserName(userName);
//
//        Message testMessage = new Message();
//        testMessage.setChat(testChat);
//
//        registrationService.registerUser(testMessage);
//
//        Optional<User> savedUserOptional = userRepository.findById(chatId);
//        assertTrue(savedUserOptional.isPresent(), "User should be saved to the database");
//
//        User savedUser = savedUserOptional.get();
//        assertEquals(chatId, savedUser.getChatId());
//        assertEquals(firstName, savedUser.getFirstName());
//        assertEquals(lastName, savedUser.getLastName());
//        assertEquals(userName, savedUser.getUserName());
//        assertEquals(ConversationState.IDLE, savedUser.getConversationState());
//    }
//
//    @Test
//    @Sql("/sql/add-test-user.sql")
//    void whenRegisterUser_andUserAlreadyExists_thenUserIsNotSavedAgain() {
//        long chatId = 12345L;
//
//        long initialCount = userRepository.count();
//        assertEquals(1, initialCount);
//
//        Chat testChat = new Chat(chatId, "private");
//        testChat.setFirstName("Oleh_NewName");
//
//        Message testMessage = new Message();
//        testMessage.setChat(testChat);
//
//        registrationService.registerUser(testMessage);
//
//        long finalCount = userRepository.count();
//        assertEquals(1, finalCount, "A new user should not have been created.");
//
//        User userAfterAct = userRepository.findById(chatId).orElseThrow();
//        assertEquals("Oleh", userAfterAct.getFirstName(), "Existing user's data should not be modified.");
//    }
//
//    @Test
//    void whenRegisteringUser_withMissingOptionalData_thenUserIsSavedWithNulls() {
//        long chatId = 54321L;
//        String firstName = "Jane";
//
//        Chat testChat = new Chat(chatId, "private");
//        testChat.setFirstName(firstName);
//        testChat.setLastName(null);
//        testChat.setUserName(null);
//
//        Message testMessage = new Message();
//        testMessage.setChat(testChat);
//
//        registrationService.registerUser(testMessage);
//
//        Optional<User> savedUserOptional = userRepository.findById(chatId);
//        assertTrue(savedUserOptional.isPresent(), "User should be saved to the database");
//
//        User savedUser = savedUserOptional.get();
//        assertEquals(chatId, savedUser.getChatId());
//        assertEquals(firstName, savedUser.getFirstName());
//        assertNull(savedUser.getLastName(), "Missing LastName should be null in the database");
//        assertNull(savedUser.getUserName(), "Missing UserName should be null in the database");
//        assertEquals(ConversationState.IDLE, savedUser.getConversationState());
//    }
//
//    @Test
//    void whenRegistering_withMessageMissingChat_thenNoUserIsCreated() {
//        long initialCount = userRepository.count();
//        assertEquals(0, initialCount);
//
//        Message messageWithoutChat = new Message();
//
//        registrationService.registerUser(messageWithoutChat);
//
//        long finalCount = userRepository.count();
//        assertEquals(initialCount, finalCount, "No user should be created if the message has no chat");
//    }
//}