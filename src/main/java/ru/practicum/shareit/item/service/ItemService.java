package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(Long userId, ItemDto itemDto);

    List<ItemDto> getAllUserItems(Long userId);

    ItemDto getItemById(Long itemId);

    List<ItemDto> searchItem(String text);

    ItemDto updateItem(Long userId, Long itemId, ItemUpdateDto updateDto);
}
