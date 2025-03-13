package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.practicum.shareit.validation.OnCreate;
import ru.practicum.shareit.validation.OnUpdate;

@Data
public class UserSaveDto {
    @NotBlank(message = "Имя пользователя не может быть пустым", groups = OnCreate.class)
    private String name;

    @NotBlank(message = "Email не может быть пустым", groups = OnCreate.class)
    @Email(message = "Email должен соответствовать формату", groups = {OnCreate.class, OnUpdate.class})
    private String email;
}
