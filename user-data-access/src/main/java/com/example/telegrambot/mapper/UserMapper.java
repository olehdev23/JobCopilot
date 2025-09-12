package com.example.telegrambot.mapper;

import com.example.dto.UserDto;
import com.example.telegrambot.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "chatId", source = "chatId")
    User toEntity(UserDto userDto);

    @Mapping(target = "chatId", source = "chatId")
    UserDto toDto(User user);
}
