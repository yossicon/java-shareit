package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingDtoJsonTest {
    private final JacksonTester<BookingDto> json;

    @Test
    void testBookingDto() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2025, 5, 5, 15, 0);

        BookingDto bookingDto = new BookingDto(
                1L,
                new UserDto(),
                new ItemDto(),
                dateTime,
                dateTime.plusDays(10),
                BookingStatus.WAITING
        );

        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2025-05-05T15:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2025-05-15T15:00:00");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(String.valueOf(BookingStatus.WAITING));
    }
}
