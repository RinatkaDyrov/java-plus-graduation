package ru.practicum.service;

public record ConfirmedRequestsChangedEvent(Long eventId, int delta) {
}
