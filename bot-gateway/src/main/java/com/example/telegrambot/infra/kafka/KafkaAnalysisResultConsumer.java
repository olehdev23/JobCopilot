package com.example.telegrambot.infra.kafka;

import com.example.dto.AnalysisResultDto;
import com.example.telegrambot.infra.telegram.MessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@Slf4j
public class KafkaAnalysisResultConsumer {

    private final MessageSender messageSender;

    public KafkaAnalysisResultConsumer(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @KafkaListener(topics = "${app.kafka.topic.analysis-results}")
    public void listen(AnalysisResultDto result) {
        log.info("Received analysis result: {}", result);
        SendMessage message = new SendMessage(String.valueOf(result.chatId()), result.resultText());
        messageSender.send(message);
    }
}
