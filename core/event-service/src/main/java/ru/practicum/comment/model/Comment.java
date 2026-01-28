package ru.practicum.comment.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.dto.event.State;
import ru.practicum.event.model.Event;

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

    @Column(nullable = false)
    String name;

    @Column(name = "title")
    String title;

    @JoinColumn(name = "creator_id", nullable = false)
    Long creatorId;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    Event event;

    @Enumerated(EnumType.STRING)
    State state;
}
