package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.util.HttpHeaderUtil;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto addRequest(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long userId,
                                     @RequestBody ItemRequestSaveDto requestSaveDto) {
        log.info("Добавление запроса {} пользователем {}", requestSaveDto.getDescription(), userId);
        ItemRequestDto addedRequest = itemRequestService.addRequest(userId, requestSaveDto);
        log.info("Запрос успешно добавлен с id {}", addedRequest.getId());
        return addedRequest;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemRequestDto> getAllUserRequests(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long userId) {
        log.info("Получение запросов пользователя с id {}", userId);
        return itemRequestService.getAllUserRequests(userId);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemRequestDto> getAllRequests(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long userId) {
        log.info("Получение всех запросов пользователем с id {}", userId);
        return itemRequestService.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemRequestDto getRequestById(@PathVariable Long requestId) {
        log.info("Получение запроса по id {}", requestId);
        return itemRequestService.getRequestById(requestId);
    }
}
