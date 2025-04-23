package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingUnavailableException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    @Transactional
    public ItemDto addItem(Long userId, ItemSaveDto itemDto) {
        User owner = findUserById(userId);
        Item item = itemMapper.mapToItem(itemDto);
        item.setOwner(owner);
        Long requestId = itemDto.getRequestId();
        if (requestId != null) {
            ItemRequest request = requestRepository.findById(requestId)
                    .orElseThrow(() -> new NotFoundException(String.format("Запрос с id %d не найден", requestId)));
            item.setRequest(request);
        }
        return itemMapper.mapToItemDto(itemRepository.save(item));
    }

    @Override
    public List<ItemDtoWithBookings> getAllUserItems(Long userId) {
        findUserById(userId);
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();
        LocalDateTime date = LocalDateTime.now();

        List<Booking> allBookings = bookingRepository.findAllByItemIdIn(itemIds);
        Map<Long, List<Booking>> bookingsByItems = allBookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId(), Collectors.toList()));

        List<Comment> allComments = commentRepository.findAllByItemIdIn(itemIds);
        Map<Long, List<Comment>> commentsByItems = allComments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId(), Collectors.toList()));

        return items.stream()
                .map(item -> {
                    List<Booking> itemBookings = bookingsByItems.getOrDefault(item.getId(), List.of());

                    Booking lastBooking = itemBookings.stream()
                            .filter(booking -> booking.getEnd().isBefore(date))
                            .max(Comparator.comparing(Booking::getStart))
                            .orElse(null);
                    Booking nextBooking = itemBookings.stream()
                            .filter(booking -> booking.getStart().isAfter(date))
                            .min(Comparator.comparing(Booking::getStart))
                            .orElse(null);
                    List<Comment> comments = commentsByItems.getOrDefault(item.getId(), List.of());
                    return itemMapper.mapToItemDtoWithBookings(item, lastBooking, nextBooking, comments);
                }).toList();
    }

    @Override
    public ItemDtoWithBookings getItemById(Long userId, Long itemId) {
        Booking lastBooking;
        Booking nextBooking;
        LocalDateTime date = LocalDateTime.now();
        Item item = findItemById(itemId);
        if (!item.getOwner().getId().equals(userId)) {
            lastBooking = null;
            nextBooking = null;
        } else {
            lastBooking = bookingRepository
                    .findFirstByItemIdAndEndBeforeOrderByStartDesc(item.getId(), date)
                    .orElse(null);
            nextBooking = bookingRepository
                    .findFirstByItemIdAndStartAfterOrderByStartAsc(item.getId(), date)
                    .orElse(null);
        }
        List<Comment> comments = commentRepository.findAllByItemId(item.getId());
        return itemMapper.mapToItemDtoWithBookings(item, lastBooking, nextBooking, comments);
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
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemSaveDto itemDto) {
        Item item = findItemById(itemId);
        findUserById(userId);
        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Обновление данных вещи доступно только её владельцу");
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return itemMapper.mapToItemDto(item);
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentSaveDto commentDto) {
        User author = findUserById(userId);
        Item item = findItemById(itemId);
        Booking booking = bookingRepository.findByItemIdAndBookerId(itemId, userId)
                .orElseThrow(() -> new BookingUnavailableException(String.format("Бронирование вещи с id %d " +
                        "пользователем с id %d не найдено", itemId, userId)));
        LocalDateTime date = LocalDateTime.now();
        if (booking.getStatus() != BookingStatus.APPROVED || !booking.getEnd().isBefore(date)) {
            throw new BookingUnavailableException(String.format("Бронирование с id %d недоступно для отзыва",
                    booking.getId()));
        }
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        return commentMapper.mapToCommentDto(commentRepository.save(comment));
    }

    private Item findItemById(Long itemId) {
        return itemRepository.findByIdWithOwner(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь с id %d не найдена",
                        itemId)));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", userId)));
    }
}
