//package com.example.telegrambot.service.kafka;
//
//import com.example.telegrambot.dto.AnalysisTask;
//import com.example.telegrambot.infra.kafka.KafkaProducerServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class KafkaProducerServiceImplTest {
//
//    @Mock
//    private KafkaTemplate<String, AnalysisTask> kafkaTemplate;
//
//    @InjectMocks
//    private KafkaProducerServiceImpl kafkaProducerService;
//
//    @Captor
//    private ArgumentCaptor<AnalysisTask> taskCaptor;
//
//    @BeforeEach
//    void setUp() {
//        ReflectionTestUtils.setField(kafkaProducerService, "analysisTaskTopic", "analysis-tasks-topic");
//    }
//
//    @Test
//    void sendAnalysisTask_shouldCallKafkaTemplateSend() {
//        AnalysisTask taskToSend = new AnalysisTask(123L, "Test Vacancy");
//
//        kafkaProducerService.sendAnalysisTask(taskToSend);
//
//        verify(kafkaTemplate, times(1)).send(eq("analysis-tasks-topic"), taskCaptor.capture());
//
//        AnalysisTask capturedTask = taskCaptor.getValue();
//        assertEquals(123L, capturedTask.chatId());
//        assertEquals("Test Vacancy", capturedTask.vacancyText());
//    }
//
//    @Test
//    void sendAnalysisTask_whenKafkaTemplateThrowsException_shouldCatchAndLogException() {
//        AnalysisTask taskToSend = new AnalysisTask(123L, "Test Vacancy");
//
//
//        when(kafkaTemplate.send(anyString(), any(AnalysisTask.class)))
//                .thenThrow(new RuntimeException("Kafka is down!"));
//
//        assertDoesNotThrow(() -> kafkaProducerService.sendAnalysisTask(taskToSend));
//
//        verify(kafkaTemplate, times(1)).send(anyString(), any(AnalysisTask.class));
//    }
//}