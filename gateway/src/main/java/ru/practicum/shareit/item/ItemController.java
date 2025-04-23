package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentSaveDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;
import ru.practicum.shareit.util.HttpHeaderUtil;
import ru.practicum.shareit.validation.OnCreate;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long userId,
                                          @Validated(OnCreate.class) @RequestBody ItemSaveDto itemDto) {
        log.info("POST /items, itemDto={}, userId={}", itemDto, userId);
        return itemClient.addItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long userId,
                                             @PathVariable Long itemId,
                                             @Valid @RequestBody CommentSaveDto commentDto) {
        log.info("POST /items/comments, commentDto={}, userId={}, itemId={}", commentDto, userId, itemId);
        return itemClient.addComment(userId, itemId, commentDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUserItems(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long userId) {
        log.info("GET /items, userId={}", userId);
        return itemClient.getAllUserItems(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long userId,
                                              @PathVariable Long itemId) {
        log.info("GET /items/{itemId}, itemId={}", itemId);
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItem(@RequestParam String text) {
        log.info("GET /items/search, text={}", text);
        return itemClient.searchItem(text);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long userId,
                                             @PathVariable Long itemId,
                                             @RequestBody ItemSaveDto itemDto) {
        log.info("PATCH /items, itemId={}, userId={}", itemId, userId);
        return itemClient.updateItem(userId, itemId, itemDto);
    }
}
