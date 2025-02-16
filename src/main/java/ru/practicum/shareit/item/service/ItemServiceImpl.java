package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserService userService;

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        userService.getUserById(userId);
        Item item = itemMapper.mapToItem(itemDto);
        return itemMapper.mapToItemDto(itemRepository.addItem(userId, item));
    }

    @Override
    public List<ItemDto> getAllUserItems(Long userId) {
        return itemRepository.getAllUserItems(userId).stream()
                .map(itemMapper::mapToItemDto)
                .toList();
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        return itemRepository.getItemById(itemId)
                .map(itemMapper::mapToItemDto)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь с id %d не найдена", itemId)));
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.searchItem(text).stream()
                .map(itemMapper::mapToItemDto)
                .toList();
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemUpdateDto updateDto) {
        userService.getUserById(userId);
        if (!itemRepository.getAllUserItems(userId).contains(itemRepository.getItemById(itemId).get())) {
            throw new NotFoundException(String.format("Вещь с id %d не найдена среди вещей пользователя с id %d",
                    itemId, userId));
        }
        ItemDto oldItem = getItemById(itemId);
        if (updateDto.getName() != null && !updateDto.getName().isBlank()) {
            oldItem.setName(updateDto.getName());
        }
        if (updateDto.getDescription() != null && !updateDto.getDescription().isBlank()) {
            oldItem.setDescription(updateDto.getDescription());
        }
        if (updateDto.getAvailable() != null) {
            oldItem.setAvailable(updateDto.getAvailable());
        }
        Item newItem = itemMapper.mapToItem(oldItem);
        return itemMapper.mapToItemDto(itemRepository.updateItem(itemId, newItem));
    }
}
