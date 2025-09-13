package com.example.telegrambot.infra.kafka;

import com.example.dto.AnalysisResultDto;

public interface KafkaProducerService {
    void sendAnalysisResult(AnalysisResultDto resultDto);
}
