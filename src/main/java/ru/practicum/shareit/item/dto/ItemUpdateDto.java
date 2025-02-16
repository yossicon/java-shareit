package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ItemUpdateDto {
    @Positive(message = "id вещи должен быть положительным")
    private Long id;

    private String name;

    private String description;

    private Boolean available;

    @Positive(message = "id пользователя должен быть положительным")
    private Long userId;
}
