package com.example.telegrambot.service;

import com.example.telegrambot.model.User;

public interface VacancyAnalysisService {
    /**
     * Analyzes a job vacancy against a user's profile (CV and preferences).
     *
     * @param user        The user whose profile will be used for comparison.
     * @param vacancyText The full text of the job description to be analyzed.
     * @return A string containing a personalized analysis, including a match score
     * and a breakdown of how the user's skills align with the requirements.
     */
    String analyze(User user, String vacancyText);
}
