package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "similarities",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event1", "event2"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSimilarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event1", nullable = false)
    private Long event1;

    @Column(name = "event2", nullable = false)
    private Long event2;

    @Column(nullable = false)
    private double similarity;

    @Column(nullable = false)
    private Instant timestamp;
}
