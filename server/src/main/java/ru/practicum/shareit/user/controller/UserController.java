package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserSaveDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@RequestBody UserSaveDto userDto) {
        log.info("Добавление пользователя {}", userDto.getName());
        UserDto addedUser = userService.addUser(userDto);
        log.info("Пользователь {} успешно добавлен с id {}", addedUser.getName(), addedUser.getId());
        return addedUser;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAllUsers() {
        log.info("Получение всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUserById(@PathVariable Long userId) {
        log.info("Получение пользователя по id {}", userId);
        return userService.getUserById(userId);
    }

    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto updateUser(@PathVariable Long userId,
                              @RequestBody UserSaveDto userDto) {
        log.info("Обновление данных пользователя с id {}", userId);
        UserDto updatedUser = userService.updateUser(userId, userDto);
        log.info("Данные пользователя {} с id {} успешно обновлены", updatedUser.getName(), updatedUser.getId());
        return updatedUser;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(@PathVariable Long userId) {
        log.info("Удаление пользователя по id {}", userId);
        userService.deleteUser(userId);
        log.info("Пользователь с id {} успешно удалён", userId);
    }
}
