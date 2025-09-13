package com.example.telegrambot.user;

import com.example.client.UserDataClient;
import com.example.dto.UserDto;
import com.example.telegrambot.exception.ProfileUpdateException;
import com.example.telegrambot.exception.UserNotFoundException;
import feign.FeignException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDataGateway {

    private final UserDataClient userDataClient;

    public Optional<UserDto> getUser(long chatId) {
        try {
            return Optional.of(userDataClient.getUserByChatId(chatId));
        } catch (FeignException.NotFound e) {
            log.warn("User not found for chatId: {}", chatId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching user {} via Feign client: {}", chatId, e.getMessage());
            return Optional.empty();
        }
    }

    public boolean registerNewUser(UserDto userDto) {
        try {
            userDataClient.registerUser(userDto);
            log.info("User with chatId {} was successfully registered.", userDto.getChatId());
            return true;
        } catch (FeignException e) {
            log.warn("User with chatId {} is already registered"
                    + " or an error occurred during registration. Details: {}",
                    userDto.getChatId(), e.getMessage());
            return false;
        }
    }

    public void updateUser(long chatId, UserDto userDto) {
        try {
            userDataClient.updateUser(chatId, userDto);
            log.info("User with chatId {} was successfully updated.", chatId);
        } catch (FeignException.NotFound e) {
            log.error("Failed to update user. User with chatId {} not found.", chatId);
            throw new UserNotFoundException("User not found for update.");
        } catch (Exception e) {
            log.error("Failed to update user with chatId {}: {}", chatId, e.getMessage());
            throw new ProfileUpdateException("Failed to update user profile.", e);
        }
    }
}
