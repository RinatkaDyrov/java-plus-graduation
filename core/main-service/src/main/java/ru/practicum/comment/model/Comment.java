package ru.practicum.comment.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 500, nullable = false)
    String text;

    @Column(nullable = false)
    LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    User creator;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    Event event;

    @Enumerated(EnumType.STRING)
    State state;
}
