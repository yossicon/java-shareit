package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSaveDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.util.HttpHeaderUtil;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingControllerTest {
    private final ObjectMapper mapper;
    @MockBean
    private final BookingService bookingService;
    private final MockMvc mvc;
    private BookingDto bookingDto;

    @BeforeEach
    public void setUp() {
        bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setBooker(new UserDto(1L, "Floyd", "wrupnk@gmail.com"));
        bookingDto.setItem(new ItemDto(1L, "Harp", "Fine Harp", true, null, null));
        bookingDto.setStart(LocalDateTime.now());
        bookingDto.setEnd(LocalDateTime.now().plusDays(10));
        bookingDto.setStatus(BookingStatus.WAITING);
    }

    @Test
    void testAddBooking() throws Exception {
        when(bookingService.addBooking(anyLong(), any(BookingSaveDto.class)))
                .thenReturn(bookingDto);

        mvc.perform(post("/bookings")
                        .header(HttpHeaderUtil.USER_ID_HEADER, bookingDto.getBooker().getId())
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(bookingDto)));

        verify(bookingService, times(1)).addBooking(anyLong(), any(BookingSaveDto.class));
    }

    @Test
    void testGetBookingById() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenReturn(bookingDto);

        mvc.perform(get("/bookings/{bookingId}", bookingDto.getId())
                        .header(HttpHeaderUtil.USER_ID_HEADER, bookingDto.getBooker().getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingDto)));

        verify(bookingService, times(1)).getBookingById(anyLong(), anyLong());
    }

    @Test
    void testGetUserBookings() throws Exception {
        List<BookingDto> bookings = List.of(bookingDto);

        when(bookingService.getUserBookings(anyLong(), any(BookingState.class)))
                .thenReturn(bookings);

        mvc.perform(get("/bookings")
                        .header(HttpHeaderUtil.USER_ID_HEADER, bookingDto.getBooker().getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookings)));

        verify(bookingService, times(1)).getUserBookings(anyLong(), any(BookingState.class));
    }

    @Test
    void testGetOwnerBookings() throws Exception {
        List<BookingDto> bookings = List.of(bookingDto);

        when(bookingService.getOwnerBookings(anyLong(), any(BookingState.class)))
                .thenReturn(bookings);

        mvc.perform(get("/bookings/owner")
                        .header(HttpHeaderUtil.USER_ID_HEADER, bookingDto.getBooker().getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookings)));

        verify(bookingService, times(1)).getOwnerBookings(anyLong(), any(BookingState.class));
    }

    @Test
    void testApproveBooking() throws Exception {
        Long ownerId = 1L;
        Long bookingId = 10L;
        Boolean approved = true;

        BookingDto expectedBooking = new BookingDto();
        expectedBooking.setId(bookingId);
        expectedBooking.setStatus(BookingStatus.APPROVED);

        when(bookingService.approveBooking(eq(ownerId), eq(bookingId), eq(approved)))
                .thenReturn(expectedBooking);

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(HttpHeaderUtil.USER_ID_HEADER, ownerId)
                        .param("approved", approved.toString())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedBooking)));

        verify(bookingService, times(1)).approveBooking(ownerId, bookingId, approved);
    }
}