package com.example.telegrambot.service.kafka;

import com.example.telegrambot.bot.MessageSender;
import com.example.telegrambot.dto.AnalysisTask;
import com.example.telegrambot.model.User;
import com.example.telegrambot.repository.UserRepository;
import com.example.telegrambot.service.analysis.VacancyAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
@Slf4j
public class KafkaConsumerService {

    private final VacancyAnalysisService vacancyAnalysisService;
    private final MessageSender messageSender;
    private final UserRepository userRepository;

    public KafkaConsumerService(VacancyAnalysisService vacancyAnalysisService,
                                MessageSender messageSender, UserRepository userRepository) {
        this.vacancyAnalysisService = vacancyAnalysisService;
        this.messageSender = messageSender;
        this.userRepository = userRepository;
    }

    @KafkaListener(topics = "${app.kafka.topic.analysis-tasks}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void listenAnalysisTasks(AnalysisTask task) {
        log.info("Received task from Kafka: {}", task.chatId());
        try {
            long chatId = task.chatId();
            String vacancyText = task.vacancyText();

            User user = userRepository.findById(chatId)
                    .orElseThrow(() -> new RuntimeException(
                            "User not found for analysis task, chatId: " + chatId));

            String analysisResult = vacancyAnalysisService.analyze(user, vacancyText);

            messageSender.send(new SendMessage(String.valueOf(chatId), analysisResult));
            log.info("Successfully processed and sent analysis for user {}", chatId);

        } catch (Exception e) {
            log.error("Failed to process analysis task from Kafka: {}", task, e);
            messageSender.send(new SendMessage(String.valueOf(task.chatId()),
                    "Sorry, an error occurred while processing your request. "
                            + "Please try again later."));
        }
    }
}
