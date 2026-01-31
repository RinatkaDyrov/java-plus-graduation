package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "views")
public class Views {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "event_id", nullable = false)
    Long eventId;

    @Column(name = "ip", nullable = false)
    String ip;
}
