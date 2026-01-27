package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.dto.event.request.Status;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EventRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "event_id")
    private Long eventId;
    @JoinColumn(name = "requester_id")
    private Long requesterId;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name = "created")
    private LocalDateTime created;
}
