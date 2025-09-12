package com.example.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for user information.
 * Used for communication between microservices.
 */
@Data
@NoArgsConstructor
public class UserDto {
    /**
     * The unique Telegram chat ID of the user.
     */
    private Long chatId;

    /**
     * The user's CV in text format.
     */
    private String cv;

    /**
     * The user's job preferences in text format.
     */
    private String preferences;

    /**
     * The first name of the user from their Telegram profile.
     */
    private String firstName;

    /**
     * The last name of the user from their Telegram profile.
     */
    private String lastName;

    /**
     * The username of the user from their Telegram profile.
     */
    private String userName;

    /**
     * The current state of the user in the conversation flow.
     */
    private ConversationState conversationState;
}
