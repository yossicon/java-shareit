package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UserUpdateDto {

    @Positive(message = "id должен быть положительным")
    private Long id;

    private String name;

    @Email(message = "Email должен соответствовать формату")
    private String email;
}
