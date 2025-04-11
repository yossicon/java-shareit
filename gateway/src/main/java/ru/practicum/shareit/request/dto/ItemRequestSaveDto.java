package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ItemRequestSaveDto {
    @NotBlank(message = "Описание запроса не может быть пустым")
    private String description;
}
