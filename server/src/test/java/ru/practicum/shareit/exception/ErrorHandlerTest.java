package ru.practicum.shareit.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingSaveDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentSaveDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.util.HttpHeaderUtil;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {BookingController.class, ItemController.class})
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ErrorHandlerTest {
    private static final Long NONEXISTENT_USER_ID = 1L;
    private static final Long NONEXISTENT_ITEM_ID = 50L;
    private static final Long NONEXISTENT_BOOKING_ID = 100L;

    private final MockMvc mockMvc;
    @MockBean
    private final BookingService bookingService;
    @MockBean
    private final ItemService itemService;
    private final ObjectMapper mapper;

    @Test
    void testHandleNotFound() throws Exception {
        when(bookingService.getBookingById(NONEXISTENT_USER_ID, NONEXISTENT_BOOKING_ID))
                .thenThrow(new NotFoundException("Бронирование не найдено"));

        mockMvc.perform(get("/bookings/{bookingId}", NONEXISTENT_BOOKING_ID)
                        .header(HttpHeaderUtil.USER_ID_HEADER, NONEXISTENT_USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Бронирование не найдено"));
    }

    @Test
    void testHandleForbidden() throws Exception {
        when(bookingService.getBookingById(NONEXISTENT_USER_ID, NONEXISTENT_BOOKING_ID))
                .thenThrow(new ForbiddenException("Ошибка доступа"));

        mockMvc.perform(get("/bookings/{bookingId}", NONEXISTENT_BOOKING_ID)
                        .header(HttpHeaderUtil.USER_ID_HEADER, NONEXISTENT_USER_ID))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Ошибка доступа"));
    }

    @Test
    void testHandleItemUnavailable() throws Exception {
        BookingSaveDto bookingSaveDto = new BookingSaveDto();
        bookingSaveDto.setStart(LocalDateTime.now().plusDays(1));
        bookingSaveDto.setStart(LocalDateTime.now().plusDays(10));

        when(bookingService.addBooking(eq(NONEXISTENT_USER_ID), any()))
                .thenThrow(new ItemUnavailableException("Вещь недоступна для бронирования"));

        mockMvc.perform(post("/bookings")
                        .header(HttpHeaderUtil.USER_ID_HEADER, NONEXISTENT_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingSaveDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Вещь недоступна для бронирования"));
    }

    @Test
    void testHandleBookingUnavailable() throws Exception {
        CommentSaveDto commentSaveDto = new CommentSaveDto();
        commentSaveDto.setText("It's comment");

        when(itemService.addComment(eq(NONEXISTENT_USER_ID), eq(NONEXISTENT_ITEM_ID), any(CommentSaveDto.class)))
                .thenThrow(new BookingUnavailableException("Бронирование недоступно"));

        mockMvc.perform(post("/items/{itemId}/comment", NONEXISTENT_ITEM_ID)
                        .header(HttpHeaderUtil.USER_ID_HEADER, NONEXISTENT_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentSaveDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Бронирование недоступно"));
    }


    @Test
    void testHandleDuplicatedData() throws Exception {
        when(bookingService.approveBooking(NONEXISTENT_USER_ID, NONEXISTENT_BOOKING_ID, true))
                .thenThrow(new DuplicatedDataException("Бронирование уже подтверждено"));

        mockMvc.perform(patch("/bookings/{bookingId}?approved=true", NONEXISTENT_BOOKING_ID)
                        .header(HttpHeaderUtil.USER_ID_HEADER, NONEXISTENT_USER_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Бронирование уже подтверждено"));
    }

    @Test
    void testHandle() throws Exception {
        when(bookingService.getBookingById(NONEXISTENT_USER_ID, NONEXISTENT_BOOKING_ID))
                .thenThrow(new RuntimeException("Непредвиденная ошибка"));

        mockMvc.perform(get("/bookings/{bookingId}", NONEXISTENT_BOOKING_ID)
                        .header(HttpHeaderUtil.USER_ID_HEADER, NONEXISTENT_USER_ID))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Непредвиденная ошибка"));
    }
}

