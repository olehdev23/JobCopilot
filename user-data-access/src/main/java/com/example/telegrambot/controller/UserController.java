package com.example.telegrambot.controller;

import com.example.dto.UserDto;
import com.example.telegrambot.mapper.UserMapper;
import com.example.telegrambot.model.User;
import com.example.telegrambot.user.UserService;
import com.example.telegrambot.user.UserServiceImpl;
import java.net.URI;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserServiceImpl userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<UserDto> getUserByChatId(@PathVariable Long chatId) {
        Optional<User> user = userService.findByChatId(chatId);
        return user.map(u -> ResponseEntity.ok(userMapper.toDto(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserDto> registerUser(
            @RequestBody UserDto userDto) {
        User savedUser = userService.registerUser(userDto);
        UserDto savedUserDto = userMapper.toDto(savedUser);

        return ResponseEntity.created(URI.create("/api/v1/users/"
                + savedUserDto.getChatId())).body(savedUserDto);
    }

    @PutMapping("/{chatId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long chatId,
                                              @RequestBody UserDto userDto) {
        if (!userService.existsById(chatId)) {
            return ResponseEntity.notFound().build();
        }
        User updatedUser = userMapper.toEntity(userDto);
        updatedUser.setChatId(chatId);
        User savedUser = userService.save(updatedUser);

        return ResponseEntity.ok(userMapper.toDto(savedUser));
    }
}
