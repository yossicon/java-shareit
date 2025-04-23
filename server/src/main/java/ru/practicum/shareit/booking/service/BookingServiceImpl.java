package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSaveDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ItemUnavailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto addBooking(Long bookerId, BookingSaveDto bookingSaveDto) {
        User booker = findUserById(bookerId);
        Item item = itemRepository.findById(bookingSaveDto.getItemId())
                .orElseThrow(() -> new NotFoundException(String.format("Вещь с id %d не найдена",
                        bookingSaveDto.getItemId())));
        if (!item.getAvailable()) {
            throw new ItemUnavailableException("Вещь недоступна для бронирования");
        }
        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(bookingSaveDto.getStart());
        booking.setEnd(bookingSaveDto.getEnd());
        booking.setStatus(BookingStatus.WAITING);
        return bookingMapper.mapToBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = findBookingById(bookingId);
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Просмотр информации о бронировании доступен " +
                    "только владельцу вещи или автору бронирования");
        }
        return bookingMapper.mapToBookingDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long bookerId, BookingState state) {
        findUserById(bookerId);
        List<Booking> bookings;
        LocalDateTime date = LocalDateTime.now();

        switch (state) {
            case ALL -> bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId);
            case CURRENT -> bookings = bookingRepository
                    .findAllCurrentBookingsByBookerId(bookerId, date);
            case PAST -> bookings = bookingRepository
                    .findAllByBookerIdAndEndBeforeOrderByStartDesc(bookerId, date);
            case FUTURE -> bookings = bookingRepository
                    .findAllByBookerIdAndStartAfterOrderByStartDesc(bookerId, date);
            case WAITING -> bookings = bookingRepository
                    .findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.WAITING);
            case REJECTED -> bookings = bookingRepository
                    .findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.REJECTED);
            default -> throw new NotFoundException(String.format("Статус %s  не найден", state));
        }
        return bookings.stream()
                .map(bookingMapper::mapToBookingDto)
                .toList();
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long ownerId, BookingState state) {
        findUserById(ownerId);
        List<Item> items = itemRepository.findAllByOwnerId(ownerId);
        LocalDateTime date = LocalDateTime.now();

        if (items.isEmpty()) {
            throw new NotFoundException(String.format("У пользователя c id %d ещё нет вещей", ownerId));
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();
        List<Booking> bookings;

        switch (state) {
            case ALL -> bookings = bookingRepository.findAllByItemIdInOrderByStartDesc(itemIds);
            case CURRENT -> bookings = bookingRepository.findAllCurrentBookingsByItemIds(itemIds, date);
            case PAST -> bookings = bookingRepository.findAllByItemIdInAndEndBeforeOrderByStartDesc(itemIds, date);
            case FUTURE -> bookings = bookingRepository.findAllByItemIdInAndStartAfterOrderByStartDesc(itemIds, date);
            case WAITING ->
                    bookings = bookingRepository.findAllByItemIdInAndStatusOrderByStartDesc(itemIds, BookingStatus.WAITING);
            case REJECTED ->
                    bookings = bookingRepository.findAllByItemIdInAndStatusOrderByStartDesc(itemIds, BookingStatus.REJECTED);
            default -> throw new NotFoundException(String.format("Статус %s не найден", state));
        }
        return bookings.stream()
                .map(bookingMapper::mapToBookingDto)
                .toList();
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long ownerId, Long bookingId, Boolean approved) {
        Booking booking = findBookingById(bookingId);
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Подтверждение/отклонение бронирования может быть выполнено " +
                    "только владельцем вещи");
        }
        if (BookingStatus.APPROVED == booking.getStatus()) {
            throw new DuplicatedDataException("Бронирование уже подтверждено");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingMapper.mapToBookingDto(booking);
    }

    private Booking findBookingById(Long bookingId) {
        return bookingRepository.findByIdWithBookerAndItem(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Бронирование с id %d не найдено", bookingId)));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", userId)));
    }
}
