package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSaveDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {
    BookingDto addBooking(Long bookerId, BookingSaveDto bookingSaveDto);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getUserBookings(Long bookerId, BookingState state);

    List<BookingDto> getOwnerBookings(Long ownerId, BookingState state);

    BookingDto approveBooking(Long ownerId, Long bookingId, Boolean approved);
}
