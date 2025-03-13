package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
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
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto addItem(Long userId, ItemSaveDto itemDto) {
        User owner = findUserById(userId);
        Item item = itemMapper.mapToItem(itemDto);
        item.setOwner(owner);
        return itemMapper.mapToItemDto(itemRepository.save(item));
    }

    @Override
    public List<ItemDtoWithBookings> getAllUserItems(Long userId) {
        findUserById(userId);
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        return items.stream()
                .map(item -> {
                    LocalDateTime date = LocalDateTime.now();
                    Booking lastBooking = bookingRepository
                            .findFirstByItemIdAndStartAfterOrderByStartAsc(item.getId(), date)
                            .orElse(null);
                    Booking nextBooking = bookingRepository
                            .findFirstByItemIdAndStartAfterOrderByStartDesc(item.getId(), date)
                            .orElse(null);
                    List<Comment> comments = commentRepository.findAllByItemId(item.getId());
                    return itemMapper.mapToItemDtoWithBookings(item, lastBooking, nextBooking, comments);
                }).toList();
    }

    @Override
    public ItemDtoWithBookings getItemById(Long itemId) {
        Item item = findItemById(itemId);
        LocalDateTime date = LocalDateTime.now();
        Booking lastBooking = bookingRepository
                .findFirstByItemIdAndStartAfterOrderByStartAsc(item.getId(), date)
                .orElse(null);
        Booking nextBooking = bookingRepository
                .findFirstByItemIdAndStartAfterOrderByStartDesc(item.getId(), date)
                .orElse(null);
        List<Comment> comments = commentRepository.findAllByItemId(item.getId());
        return itemMapper.mapToItemDtoWithBookings(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.searchItem(text).stream()
                .filter(Item::getAvailable)
                .map(itemMapper::mapToItemDto)
                .toList();
    }

    @Override
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
        return itemMapper.mapToItemDto(itemRepository.save(item));
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentSaveDto commentDto) {
        User author = findUserById(userId);
        Item item = findItemById(itemId);
        Booking booking = bookingRepository.findByItemIdAndBookerId(itemId, userId)
                .orElseThrow(() -> new BookingUnavailableException(String.format("Бронирование вещи с id %d " +
                        "пользователем с id %d не найдено", itemId, userId)));
        LocalDateTime date = LocalDateTime.now();
        if (!booking.getStatus().equals(Status.APPROVED) || !booking.getEnd().isBefore(date)) {
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

    public Item findItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь с id %d не найдена",
                        itemId)));
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", userId)));
    }
}
