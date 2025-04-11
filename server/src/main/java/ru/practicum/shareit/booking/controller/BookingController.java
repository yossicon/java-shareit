package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSaveDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.util.HttpHeaderUtil;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto addBooking(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long bookerId,
                                 @RequestBody BookingSaveDto bookingSaveDto) {
        log.info("Бронирование вещи с id {} пользователем с id {}", bookingSaveDto.getItemId(), bookerId);
        BookingDto addedBooking = bookingService.addBooking(bookerId, bookingSaveDto);
        log.info("Бронирование успешно добавлено с id {}", addedBooking.getId());
        return addedBooking;
    }

    @GetMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public BookingDto getBookingById(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long userId,
                                     @PathVariable Long bookingId) {
        log.info("Получение информации по бронированию с id {}", bookingId);
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<BookingDto> getUserBookings(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long bookerId,
                                            @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("Получение списка бронирований пользователя с id {}", bookerId);
        return bookingService.getUserBookings(bookerId, state);
    }

    @GetMapping("/owner")
    @ResponseStatus(HttpStatus.OK)
    public List<BookingDto> getOwnerBookings(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long ownerId,
                                             @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("Получение списка бронирований для всех вещей пользователя с id {}", ownerId);
        return bookingService.getOwnerBookings(ownerId, state);
    }

    @PatchMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public BookingDto approveBooking(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long ownerId,
                                     @PathVariable Long bookingId,
                                     @RequestParam Boolean approved) {
        log.info("Изменение статуса бронирования с id {}", bookingId);
        BookingDto bookingDto = bookingService.approveBooking(ownerId, bookingId, approved);
        log.info("Статус бронирования с id {}: {}", bookingId, bookingDto.getStatus());
        return bookingDto;
    }
}
