package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.category.model.Category;
import ru.practicum.location.model.Location;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "events", indexes = {
        //вроде как так поиск по текстовому запросу должен быстрее работать, но я не уверена, что правильно это применила
        @Index(name = "idx_event_annotation", columnList = "annotation"),
        @Index(name = "idx_event_description", columnList = "description")
})
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "title", nullable = false)
    String title;

    //краткая аннотация
    @Column(name = "annotation")
    String annotation;

    //полное описание
    @Column(name = "description", nullable = false)
    String description;

    //дата мероприятия
    @Column(name = "event_date", nullable = false)
    LocalDateTime eventDate;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "location_id")
    Location location;

    //платно или бесплатно, default = false
    @Column(name = "paid")
    Boolean paid;

    //лимит участников, default = 0, при значении 0 количество не ограничено
    @Column(name = "participant_limit")
    int participantLimit;

    //премодерация заявок, false = заявки принимаются автоматически, true = заявки требуют подтверждения
    @Column(name = "request_moderation")
    Boolean requestModeration;

    @ManyToOne
    @JoinColumn(name = "initiator_id")
    User initiator;

    //счетчик просмотров
    @Column(name = "views")
    int views;

    @Enumerated(EnumType.STRING)
    State state;

    // все ниже добавлено для фулл дто
    @Column(name = "confirmed_requests")
    int confirmedRequests;

    @Column(name = "created_on")
    LocalDateTime createdOn;

    @Column(name = "published_on")
    LocalDateTime publishedOn;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return participantLimit == event.participantLimit && views == event.views
                && confirmedRequests == event.confirmedRequests && Objects.equals(id, event.id)
                && Objects.equals(title, event.title) && Objects.equals(annotation, event.annotation)
                && Objects.equals(description, event.description) && Objects.equals(eventDate, event.eventDate)
                && Objects.equals(category, event.category) && Objects.equals(location, event.location)
                && Objects.equals(paid, event.paid) && Objects.equals(requestModeration, event.requestModeration)
                && Objects.equals(initiator, event.initiator) && state == event.state
                && Objects.equals(createdOn, event.createdOn) && Objects.equals(publishedOn, event.publishedOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, annotation, description, eventDate, category, location, paid, participantLimit,
                requestModeration, initiator, views, state, confirmedRequests, createdOn, publishedOn);
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", annotation='" + annotation + '\'' +
                ", description='" + description + '\'' +
                ", eventDate=" + eventDate +
                ", category=" + category +
                ", location=" + location +
                ", paid=" + paid +
                ", participantLimit=" + participantLimit +
                ", requestModeration=" + requestModeration +
                ", initiator=" + initiator +
                ", views=" + views +
                ", state=" + state +
                ", confirmedRequests=" + confirmedRequests +
                ", createdOn=" + createdOn +
                ", publishedOn=" + publishedOn +
                '}';
    }
}
