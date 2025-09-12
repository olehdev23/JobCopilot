package com.example.telegrambot.user;

import com.example.dto.UserDto;
import com.example.telegrambot.mapper.UserMapper;
import com.example.telegrambot.model.User;
import com.example.telegrambot.repository.UserRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User registerUser(UserDto dto) {
        Optional<User> existingUser = userRepository.findById(dto.getChatId());
        if (existingUser.isEmpty()) {
            User user = userMapper.toEntity(dto);
            User savedUser = userRepository.save(user);
            log.info("User was created for chatId: {}", dto.getChatId());
            return savedUser;
        }
        log.info("User with chatId: {} already exists. Skipping registration.", dto.getChatId());
        return existingUser.get();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByChatId(Long chatId) {
        return userRepository.findById(chatId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long chatId) {
        return userRepository.existsById(chatId);
    }
}
