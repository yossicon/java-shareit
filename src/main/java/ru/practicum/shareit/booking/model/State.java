package ru.practicum.shareit.booking.model;

public enum State {
    WAITING,
    APPROVED,
    REJECTED,
    ALL,
    PAST,
    FUTURE,
    CURRENT;

    public static State from(String state) {
        return switch (state.toLowerCase()) {
            case "waiting" -> WAITING;
            case "approved" -> APPROVED;
            case "rejected" -> REJECTED;
            case "all" -> ALL;
            case "past" -> PAST;
            case "future" -> FUTURE;
            case "current" -> CURRENT;
            default -> throw new IllegalStateException("Unexpected value: " + state.toLowerCase());
        };
    }
}
