package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto addUser(UserDto userDto) {
        User user = UserMapper.mapToUser(userDto);
        if (userRepository.getUserByEmail(user.getEmail()).isPresent()) {
            throw new DuplicatedDataException(String.format("Email %s уже используется", user.getEmail()));
        }
        return UserMapper.mapToUserDto(userRepository.addUser(user));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.getAllUsers().stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    @Override
    public UserDto getUserById(Long userId) {
        return userRepository.getUserById(userId)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", userId)));
    }

    @Override
    public UserDto updateUser(Long userId, UserUpdateDto updateDto) {
        if (userRepository.getUserByEmail(updateDto.getEmail()).isPresent()) {
            throw new DuplicatedDataException(String.format("Email %s уже используется", updateDto.getEmail()));
        }
        UserDto oldUser = getUserById(userId);
        if (updateDto.getEmail() != null) {
            oldUser.setEmail(updateDto.getEmail());
        }
        if (updateDto.getName() != null) {
            oldUser.setName(updateDto.getName());
        }
        User newUser = UserMapper.mapToUser(oldUser);
        return UserMapper.mapToUserDto(userRepository.updateUser(userId, newUser));
    }

    @Override
    public void deleteUser(Long userId) {
        getUserById(userId);
        userRepository.deleteUser(userId);
    }
}
