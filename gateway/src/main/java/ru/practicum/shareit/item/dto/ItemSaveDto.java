package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.validation.OnCreate;

@Data
public class ItemSaveDto {
    @NotBlank(message = "Название вещи не может быть пустым", groups = OnCreate.class)
    private String name;

    @NotBlank(message = "Описание вещи не может быть пустым", groups = OnCreate.class)
    private String description;

    @NotNull(message = "Доступность вещи должна быть указана", groups = OnCreate.class)
    private Boolean available;

    private Long requestId;
}
