package com.example.telegrambot.exception;

/**
 * Custom exception thrown when a problem occurs during a user profile update via a REST API call.
 */
public class ProfileUpdateException extends RuntimeException {

    /**
     * Constructs a new ProfileUpdateException with the specified detail message and cause.
     * * @param message The detail message.
     * @param cause The cause (e.g., a RestClientException), which is saved for later retrieval.
     */
    public ProfileUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
