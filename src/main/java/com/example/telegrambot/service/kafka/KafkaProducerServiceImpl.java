package com.example.telegrambot.service.kafka;

import com.example.telegrambot.dto.AnalysisTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, AnalysisTask> kafkaTemplate;
    private final String analysisTaskTopic;

    public KafkaProducerServiceImpl(KafkaTemplate<String, AnalysisTask> kafkaTemplate,
                                    @Value("${app.kafka.topic.analysis-tasks}")
                                    String analysisTaskTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.analysisTaskTopic = analysisTaskTopic;
    }

    @Override
    public void sendAnalysisTask(AnalysisTask task) {
        try {
            kafkaTemplate.send(analysisTaskTopic, task);
            log.info("Sent analysis task to Kafka topic '{}': {}", analysisTaskTopic, task);
        } catch (Exception e) {
            log.error("Failed to send analysis task to Kafka: {}", task, e);
        }
    }
}
