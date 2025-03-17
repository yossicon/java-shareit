package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserSaveDto;

import java.util.List;

public interface UserService {
    UserDto addUser(UserSaveDto userDto);

    List<UserDto> getAllUsers();

    UserDto getUserById(Long userId);

    UserDto updateUser(Long userId, UserSaveDto userDto);

    void deleteUser(Long userId);
}
