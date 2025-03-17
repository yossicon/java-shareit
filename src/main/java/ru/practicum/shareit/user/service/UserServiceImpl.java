package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserSaveDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto addUser(UserSaveDto userDto) {
        User user = userMapper.mapToUser(userDto);
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicatedDataException(String.format("Email %s уже используется", user.getEmail()));
        }
        return userMapper.mapToUserDto(userRepository.save(user));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::mapToUserDto)
                .toList();
    }

    @Override
    public UserDto getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", userId)));
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserSaveDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new DuplicatedDataException(String.format("Email %s уже используется", userDto.getEmail()));
        }
        User user = findUserById(userId);
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }
        return userMapper.mapToUserDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        findUserById(userId);
        userRepository.deleteById(userId);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", userId)));
    }
}
