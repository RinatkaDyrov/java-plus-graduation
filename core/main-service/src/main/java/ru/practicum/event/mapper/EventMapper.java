package ru.practicum.event.mapper;

import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.eventRequest.dto.NewEventRequest;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.location.mapper.LocationMapper;
import ru.practicum.location.model.Location;
import ru.practicum.user.mappers.UserMapper;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

public class EventMapper {
    public static Event mapToEventNew(NewEventRequest request, Category category,
                                      Location location, User user) {
        Event event = Event.builder()
                .title(request.getTitle())
                .annotation(request.getAnnotation())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .category(category)
                .location(location)
                .paid(request.getPaid())
                .requestModeration(request.getRequestModeration())
                .initiator(user)
                .state(State.PENDING)
                .createdOn(LocalDateTime.now())
                .views(0)
                .build();
        if (request.hasParticipantLimit()) {
            event.setParticipantLimit(request.getParticipantLimit());
        } else {
            event.setParticipantLimit(0);
        }
        return event;
    }

    public static EventShortDto mapToShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.mapToDto(event.getCategory()))
                .eventDate(event.getEventDate())
                .initiator(UserMapper.mapToUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .description(event.getDescription())
                .participantLimit(event.getParticipantLimit())
                .state(event.getState())
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .location(LocationMapper.mapToDto(event.getLocation()))
                .requestModeration(event.getRequestModeration())
                .confirmedRequests(event.getConfirmedRequests())
                .build();
    }

    public static EventFullDto mapToFullDto(Event event) {
        return EventFullDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.mapToDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiator(UserMapper.mapToUserShortDto(event.getInitiator()))
                .location(LocationMapper.mapToDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }
}
