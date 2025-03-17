package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import ru.practicum.shareit.validation.StartBeforeEnd;

import java.time.LocalDateTime;

@Data
@StartBeforeEnd
public class BookingSaveDto {
    @NotNull(message = "id вещи должен быть указан")
    @Positive(message = "id вещи должен быть положительным")
    private Long itemId;

    @NotNull(message = "Дата начала бронирования должна быть указана")
    @FutureOrPresent(message = "Дата начала бронирования не может быть в прошлом")
    private LocalDateTime start;

    @NotNull(message = "Дата конца бронирования должна быть указана")
    @Future(message = "Дата конца бронирования не может быть в прошлом")
    private LocalDateTime end;
}
