package com.example.telegrambot.service.profile;

import com.example.telegrambot.model.ConversationState;
import com.example.telegrambot.model.User;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.file.FileParserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileParserService fileParserService;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Test
    void whenAwaitingCv_andReceivesValidDocument_thenCvIsSavedAndStateChanges() throws TelegramApiException, IOException {
        long chatId = 123L;
        String parsedCvText = "This is a parsed CV text.";

        User testUser = new User();
        testUser.setChatId(chatId);
        testUser.setConversationState(ConversationState.AWAITING_CV);

        Document testDocument = new Document();
        testDocument.setFileName("cv.pdf");

        Message testMessage = new Message();
        testMessage.setDocument(testDocument);
        testMessage.setChat(new Chat(chatId, "private"));

        when(fileParserService.parse(testDocument)).thenReturn(parsedCvText);

        SendMessage responseMessage = profileService.processMessage(testUser, testMessage);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(ConversationState.AWAITING_PREFERENCES, savedUser.getConversationState());
        assertEquals(parsedCvText, savedUser.getCv());

        assertNotNull(responseMessage);
    }

    @Test
    void whenAwaitingPreferences_andReceivesText_thenPreferencesAreSavedAndStateChangesToIdle() {
        long chatId = 456L;
        String preferencesText = "I want to work with Java and Spring remotely.";

        User testUser = new User();
        testUser.setChatId(chatId);
        testUser.setConversationState(ConversationState.AWAITING_PREFERENCES);

        Message testMessage = new Message();
        testMessage.setText(preferencesText);
        testMessage.setChat(new Chat(chatId, "private"));

        SendMessage responseMessage = profileService.processMessage(testUser, testMessage);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User user = userCaptor.getValue();
        assertEquals(ConversationState.IDLE, user.getConversationState());

        assertEquals(preferencesText, user.getPreferences());
        assertNotNull(responseMessage);
    }

    @Test
    void whenAwaitingCv_andReceivesTextInsteadOfDocument_thenReturnsErrorMessage() {
        long chatId = 456L;
        User testUser = new User();
        testUser.setChatId(chatId);
        testUser.setConversationState(ConversationState.AWAITING_CV);

        String text = "text instead of document";

        Message testMessage = new Message();
        testMessage.setText(text);
        testMessage.setChat(new Chat(chatId, "private"));

        SendMessage sendMessage = profileService.processMessage(testUser, testMessage);

        assertNotNull(sendMessage);
        assertEquals("This step requires a file. Please send your CV as a .pdf or .docx file.", sendMessage.getText());
        verifyNoInteractions(userRepository);
        verifyNoInteractions(fileParserService);
    }

    @Test
    void whenFileParserThrowsException_thenReturnsErrorMessageAndStateIsNotChanged() throws TelegramApiException, IOException {
        long chatId = 789L;
        User testUser = new User();
        testUser.setChatId(chatId);
        testUser.setConversationState(ConversationState.AWAITING_CV);

        Document testDocument = new Document();
        Message testMessage = new Message();
        testMessage.setDocument(testDocument);
        testMessage.setChat(new Chat(chatId, "private"));

        when(fileParserService.parse(any(Document.class)))
                .thenThrow(new RuntimeException("Simulated parsing error!"));

        SendMessage responseMessage = profileService.processMessage(testUser, testMessage);

        verifyNoInteractions(userRepository);

        assertNotNull(responseMessage);
        assertTrue(responseMessage.getText().contains("Sorry, I failed to process your file."));
    }

    @Test
    void whenAwaitingPreferences_andReceivesDocumentInsteadOfText_thenReturnsErrorMessage() {
        long chatId = 123L;

        User testUser = new User();
        testUser.setChatId(chatId);
        testUser.setConversationState(ConversationState.AWAITING_PREFERENCES);

        Document testDocument = new Document();
        testDocument.setFileName("cv.pdf");

        Message testMessage = new Message();
        testMessage.setDocument(testDocument);
        testMessage.setChat(new Chat(chatId, "private"));

        SendMessage responseMessage = profileService.processMessage(testUser, testMessage);

        verifyNoInteractions(userRepository);
        verifyNoInteractions(fileParserService);

        assertEquals("Please provide your preferences as a text message.", responseMessage.getText());
        assertNotNull(responseMessage);
    }
}