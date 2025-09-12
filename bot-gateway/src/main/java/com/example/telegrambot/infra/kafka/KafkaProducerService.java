package com.example.telegrambot.infra.kafka;

import com.example.dto.AnalysisTaskDto;

public interface KafkaProducerService {
    void sendAnalysisTask(AnalysisTaskDto task);
}
