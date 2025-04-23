package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingSaveDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.util.HttpHeaderUtil;


@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long bookerId,
                                             @RequestBody @Valid BookingSaveDto bookingSaveDto) {
        log.info("POST /bookings, bookingDto={}, userId={}", bookingSaveDto, bookerId);
        return bookingClient.bookItem(bookerId, bookingSaveDto);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) long bookerId,
                                                  @RequestParam(name = "state", defaultValue = "all") String stateParam) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("GET /bookings, state={}, bookerId={}", stateParam, bookerId);
        return bookingClient.getUserBookings(bookerId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) long ownerId,
                                                   @RequestParam(name = "state", defaultValue = "all") String stateParam) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("GET /bookings/owner, state={}, ownerId={}", state, ownerId);
        return bookingClient.getOwnerBookings(ownerId, state);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long userId,
                                                 @PathVariable Long bookingId) {
        log.info("GET bookings/{bookingId}, bookingId={}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader(HttpHeaderUtil.USER_ID_HEADER) Long ownerId,
                                                 @PathVariable Long bookingId,
                                                 @RequestParam Boolean approved) {
        log.info("PATCH bookings/{bookingId}, bookingId={}, ownerId={}", bookingId, ownerId);
        return bookingClient.approveBooking(ownerId, bookingId, approved);
    }
}
