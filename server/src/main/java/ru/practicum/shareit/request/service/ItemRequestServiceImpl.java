package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository requestRepository;
    private final ItemRequestMapper requestMapper;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemRequestDto addRequest(Long userId, ItemRequestSaveDto requestSaveDto) {
        User requester = findUserById(userId);
        ItemRequest itemRequest = requestMapper.mapToItemRequest(requestSaveDto);
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now());
        return requestMapper.mapToItemRequestDto(requestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDto> getAllUserRequests(Long userId) {
        List<ItemRequest> requests = requestRepository.findAllByRequesterIdOrderByCreatedDesc(userId);
        return getRequestsWithResponses(requests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        List<ItemRequest> requests = requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(userId);
        return getRequestsWithResponses(requests);
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId) {
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Запрос с id %d не найден", requestId)));
        List<Item> requestItems = itemRepository.findAllByRequestId(requestId);
        List<ItemResponseDto> responses = itemMapper.mapToItemResponseDto(requestItems);
        ItemRequestDto requestDto = requestMapper.mapToItemRequestDto(request);
        requestDto.setItems(responses);
        return requestDto;
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", userId)));
    }

    private List<ItemRequestDto> getRequestsWithResponses(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();
        List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);
        Map<Long, List<Item>> itemsByRequests = items.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId(), Collectors.toList()));

        return requests.stream()
                .map(request -> {
                    List<Item> requestItems = itemsByRequests.getOrDefault(request.getId(), List.of());
                    List<ItemResponseDto> responses = itemMapper.mapToItemResponseDto(requestItems);
                    ItemRequestDto requestDto = requestMapper.mapToItemRequestDto(request);
                    requestDto.setItems(responses);
                    return requestDto;
                }).toList();
    }
}
