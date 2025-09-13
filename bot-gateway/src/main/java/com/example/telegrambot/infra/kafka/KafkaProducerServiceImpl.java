package com.example.telegrambot.infra.kafka;

import com.example.dto.AnalysisTaskDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, AnalysisTaskDto> kafkaTemplate;
    private final String analysisTaskTopic;

    public KafkaProducerServiceImpl(KafkaTemplate<String, AnalysisTaskDto> kafkaTemplate,
                                    @Value("${app.kafka.topic.analysis-tasks}")
                                    String analysisTaskTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.analysisTaskTopic = analysisTaskTopic;
    }

    @Override
    public void sendAnalysisTask(AnalysisTaskDto task) {
        try {
            kafkaTemplate.send(analysisTaskTopic, task);
            log.info("Sent analysis task to Kafka topic '{}': {}", analysisTaskTopic, task);
        } catch (Exception e) {
            log.error("Failed to send analysis task to Kafka: {}", task, e);
        }
    }
}
