package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ItemDto {

    @Positive(message = "id вещи должен быть положительным")
    private Long id;

    @NotBlank(message = "Название вещи не может быть пустым")
    private String name;

    @NotBlank(message = "Описание вещи не может быть пустым")
    private String description;

    @NotNull
    private Boolean available;

    @Positive(message = "id пользователя должен быть положительным")
    private Long userId;
}
