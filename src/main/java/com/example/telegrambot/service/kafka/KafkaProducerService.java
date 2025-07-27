package com.example.telegrambot.service.kafka;

import com.example.telegrambot.dto.AnalysisTask;

public interface KafkaProducerService {
    void sendAnalysisTask(AnalysisTask task);
}
