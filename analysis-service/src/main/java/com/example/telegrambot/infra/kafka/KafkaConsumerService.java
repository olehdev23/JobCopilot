package com.example.telegrambot.infra.kafka;

import com.example.dto.AnalysisTaskDto;
import com.example.telegrambot.analysis.VacancyAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {
    private final VacancyAnalysisService vacancyAnalysisService;
    @Value("${microservices.user-data-access.url}")
    private String userDataAccessUrl;

    public KafkaConsumerService(VacancyAnalysisService vacancyAnalysisService) {
        this.vacancyAnalysisService = vacancyAnalysisService;
    }

    @KafkaListener(topics = "${app.kafka.topic.analysis-tasks}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listenAnalysisTasks(AnalysisTaskDto task) {
        long chatId = task.chatId();
        log.info("Received task from Kafka for chatId: {}", chatId);

        try {
            vacancyAnalysisService.analyze(task);
            log.info("Successfully processed and sent analysis for user {}", chatId);
        } catch (Exception e) {
            log.error("Unexpected error processing analysis task from Kafka: {}", task, e);
        }
    }
}
