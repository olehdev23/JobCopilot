package com.example.dto;

import java.time.Instant;

public record AnalysisTaskDto(String taskId, long chatId,
                              Instant timestamp, String vacancyText) {
}
