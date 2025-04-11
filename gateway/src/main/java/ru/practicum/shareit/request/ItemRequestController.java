package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.util.HttpHeaderUtil;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> addRequest(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long userId,
                                             @RequestBody @Valid ItemRequestSaveDto requestSaveDto) {
        log.info("POST /requests, requestDto={}, userId={}", requestSaveDto, userId);
        return requestClient.addRequest(userId, requestSaveDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUserRequests(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long userId) {
        log.info("GET /requests, userId={}", userId);
        return requestClient.getAllUserRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long userId) {
        log.info("GET /requests/all, userId={}", userId);
        return requestClient.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@PathVariable Long requestId) {
        log.info("GET requests/{requestId}, requestId={}", requestId);
        return requestClient.getRequestById(requestId);
    }
}
