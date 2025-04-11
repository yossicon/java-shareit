package ru.practicum.shareit.item.dto;

import lombok.Data;

@Data
public class ItemResponseDto {
    private Long itemId;

    private String name;

    private Long ownerId;
}
