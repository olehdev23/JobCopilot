package com.example.dto;

public enum ConversationState {
    /**
     * The bot is not in a specific conversation flow and is awaiting a command.
     */
    IDLE,

    /**
     * The bot is awaiting the user to upload their CV file.
     */
    AWAITING_CV,

    /**
     * The bot is awaiting the user's job preferences as a text message.
     */
    AWAITING_PREFERENCES,

    /**
     * The bot is awaiting a job vacancy description to start the analysis.
     */
    AWAITING_VACANCY;
}
