package com.example.telegrambot.service.kafka;

import com.example.telegrambot.bot.MyTelegramBot;
import com.example.telegrambot.dto.AnalysisTask;
import com.example.telegrambot.model.User;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.analysis.VacancyAnalysisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {

    @Mock
    private VacancyAnalysisService vacancyAnalysisService;
    @Mock
    private MyTelegramBot messageSender;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    @Test
    void listenAnalysisTasks_whenTaskIsValid_thenProcessesAndSendsResult() {
        long chatId = 123L;
        AnalysisTask task = new AnalysisTask(chatId, "Java Developer vacancy");
        User testUser = new User();
        testUser.setChatId(chatId);

        when(userRepository.findById(chatId)).thenReturn(Optional.of(testUser));
        when(vacancyAnalysisService.analyze(any(User.class), anyString())).thenReturn("Successful analysis result.");

        kafkaConsumerService.listenAnalysisTasks(task);

        verify(vacancyAnalysisService, times(1)).analyze(any(User.class), eq("Java Developer vacancy"));

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(messageSender, times(1)).send(messageCaptor.capture());
        assertTrue(messageCaptor.getValue().getText().contains("Successful analysis result."));
    }

    @Test
    void listenAnalysisTasks_whenUserIsNotFound_shouldSendErrorMessage() {
        long nonExistentChatId = 404L;
        AnalysisTask task = new AnalysisTask(nonExistentChatId, "Vacancy for non-existent user");

        when(userRepository.findById(nonExistentChatId)).thenReturn(Optional.empty());

        kafkaConsumerService.listenAnalysisTasks(task);

        verifyNoInteractions(vacancyAnalysisService);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(messageSender, times(1)).send(messageCaptor.capture());

        SendMessage sentMessage = messageCaptor.getValue();
        assertEquals(String.valueOf(nonExistentChatId), sentMessage.getChatId());
        assertTrue(sentMessage.getText().contains("Sorry, an error occurred"));
    }

    @Test
    void listenAnalysisTasks_whenAnalysisServiceFails_shouldSendErrorMessage() {
        long chatId = 789L;
        AnalysisTask task = new AnalysisTask(chatId, "Some vacancy");
        User testUser = new User();
        testUser.setChatId(chatId);

        when(userRepository.findById(chatId)).thenReturn(Optional.of(testUser));

        when(vacancyAnalysisService.analyze(any(User.class), anyString()))
                .thenThrow(new RuntimeException("Simulated AI service failure!"));

        kafkaConsumerService.listenAnalysisTasks(task);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(messageSender, times(1)).send(messageCaptor.capture());

        SendMessage sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getText().contains("Sorry, an unexpected error occurred"));
    }
}