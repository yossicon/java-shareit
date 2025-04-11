package ru.practicum.shareit.request.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Getter
@Setter
@ToString(exclude = "requester")
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    private LocalDateTime created;
}
