package com.example.telegrambot.infra.kafka;

import com.example.dto.AnalysisResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, AnalysisResultDto> kafkaTemplate;

    @Value("${app.kafka.topic.analysis-results}")
    private String resultsTopic;

    public KafkaProducerServiceImpl(KafkaTemplate<String, AnalysisResultDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void sendAnalysisResult(AnalysisResultDto resultDto) {
        log.info("Sending analysis result to Kafka: {}", resultDto);
        try {
            kafkaTemplate.send(resultsTopic, resultDto);
        } catch (Exception e) {
            log.error("Failed to send analysis result to Kafka", e);
        }
    }
}
