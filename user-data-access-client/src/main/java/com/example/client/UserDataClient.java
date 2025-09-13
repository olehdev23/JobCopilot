package com.example.client;

import com.example.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
* Declarative client for interacting with the user-data-access microservice.
* This client provides a type-safe way to access the User REST API.
*/
@FeignClient(name = "user-data-access",
        url = "${microservices.user-data-access.url}")
public interface UserDataClient {

    /**
     * Retrieves user data by their Telegram chat ID.
     *
     * @param chatId The unique Telegram chat ID of the user.
     * @return {@link UserDto} containing the user's data.
     */
    @GetMapping("/api/v1/users/{chatId}")
    UserDto getUserByChatId(@PathVariable("chatId") long chatId);

    /**
     * Updates an existing user's data.
     *
     * @param chatId The unique Telegram chat ID of the user to be updated.
     * @param userDto The object containing the updated user data.
     * @return {@link UserDto} with the updated user details.
     */
    @PutMapping("/api/v1/users/{chatId}")
    UserDto updateUser(@PathVariable("chatId") long chatId,
                       @RequestBody UserDto userDto);

    /**
     * Registers a new user.
     *
     * @param userDto The object with the new user's data.
     * @return {@link UserDto} of the newly registered user.
     */
    @PostMapping("/api/v1/users")
    UserDto registerUser(@RequestBody UserDto userDto);
}
