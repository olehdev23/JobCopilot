package com.example.telegrambot.user;

import com.example.dto.UserDto;
import com.example.telegrambot.model.User;
import java.util.Optional;

/**
 * Service for managing all business operations related to the User entity.
 */
public interface UserService {

    /**
     * Registers a new user if they don't already exist.
     *
     * @param userDto DTO containing the user data for registration.
     * @return The registered User entity.
     */
    User registerUser(UserDto userDto);

    /**
     * Finds a user by their unique Telegram chat ID.
     *
     * @param chatId The user's Telegram chat ID.
     * @return An Optional containing the User, or an empty Optional if not found.
     */
    Optional<User> findByChatId(Long chatId);

    /**
     * Checks if a user with the given chat ID exists.
     *
     * @param chatId The chat ID to check.
     * @return True if the user exists, otherwise false.
     */
    boolean existsById(Long chatId);

    /**
     * Saves (creates or updates) a user entity in the database.
     *
     * @param user The User entity to be saved.
     * @return The saved User entity.
     */
    User save(User user);
}
