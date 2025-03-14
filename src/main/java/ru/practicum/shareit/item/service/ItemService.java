package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {
    ItemDto addItem(Long userId, ItemSaveDto itemDto);

    List<ItemDtoWithBookings> getAllUserItems(Long userId);

    ItemDtoWithBookings getItemById(Long userId, Long itemId);

    List<ItemDto> searchItem(String text);

    ItemDto updateItem(Long userId, Long itemId, ItemSaveDto itemDto);

    CommentDto addComment(Long userId, Long itemId, CommentSaveDto commentDto);
}
