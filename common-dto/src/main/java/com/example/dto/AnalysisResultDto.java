package com.example.dto;

import java.time.Instant;

public record AnalysisResultDto(String taskId, long chatId,
                                Instant timestamp, String resultText) {
}
