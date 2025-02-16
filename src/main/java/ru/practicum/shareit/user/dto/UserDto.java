package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UserDto {

    @Positive(message = "id пользователя должен быть положительным")
    private Long id;

    @NotBlank(message = "Имя пользователя не может быть пустым")
    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен соответствовать формату")
    private String email;
}
