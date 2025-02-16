package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.util.HttpHeaderUtil;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto addItem(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long userId,
                           @Valid @RequestBody ItemDto itemDto) {
        log.info("Добавление вещи {} пользователем с id {}", itemDto.getName(), userId);
        ItemDto addedItem = itemService.addItem(userId, itemDto);
        log.info("Вещь успешно добавлена с id {}", addedItem.getId());
        return addedItem;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> getAllUserItems(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long userId) {
        log.info("Получение всех вещей пользователя с id {}", userId);
        return itemService.getAllUserItems(userId);
    }

    @GetMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDto getItemById(@PathVariable Long itemId) {
        log.info("Получение вещи по id {}", itemId);
        return itemService.getItemById(itemId);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> searchItem(@RequestParam String text) {
        log.info("Поиск вещи по ключу {}", text);
        return itemService.searchItem(text);
    }

    @PatchMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDto updateItem(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long userId,
                              @PathVariable Long itemId,
                              @RequestBody ItemUpdateDto updateDto) {
        log.info("Обновление данных вещи с id {} пользователя с id {}", itemId, userId);
        ItemDto updatedItem = itemService.updateItem(userId, itemId, updateDto);
        log.info("Данные вещи {} с id {} успешно обновлены", updatedItem.getName(), updatedItem.getId());
        return updatedItem;
    }
}
