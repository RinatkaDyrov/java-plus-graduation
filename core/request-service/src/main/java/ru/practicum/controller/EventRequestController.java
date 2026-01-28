package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.request.EventRequestDto;
import ru.practicum.dto.event.request.EventRequestUpdateDto;
import ru.practicum.dto.event.request.EventRequestUpdateResult;
import ru.practicum.service.EventRequestService;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class EventRequestController {
    private final EventRequestService eventRequestService;

    @GetMapping("/requests")
    public List<EventRequestDto> getUsersEventList(@PathVariable Long userId) {
        log.info("Получение запросов на участие в событии пользователя с id {}", userId);
        return eventRequestService.getUsersRequests(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public EventRequestDto createUserRequestToEvent(@PathVariable Long userId,
                                                    @RequestParam Long eventId) {
        log.info("Создание запроса на участие события с id: {} пользователем id: {}", eventId, userId);
        return eventRequestService.createRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public EventRequestDto cancelUserRequestToEvent(@PathVariable Long userId,
                                                    @PathVariable Long requestId) {
        log.info("Отмена запроса с id: {} пользователемс id: {}", requestId, userId);
        return eventRequestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<EventRequestDto> getRequestByEvent(@PathVariable("userId") long userId,
                                                   @PathVariable("eventId") long eventId) {
        log.info("Получение информации о заявке на участие в Event id={} от пользователя id={}", eventId, userId);
        return eventRequestService.getAllByEventId(userId, eventId);
    }


    @PatchMapping("/events/{eventId}/requests")
    public EventRequestUpdateResult updateRequestStatus(@PathVariable Long userId,
                                                        @PathVariable Long eventId,
                                                        @RequestBody EventRequestUpdateDto request) {
        log.info("___Начинаем обработку запроса обновления {}", request);
        return eventRequestService.updateRequestState(userId, eventId, request);
    }

    @GetMapping("/requests/{requestId}/internal")
    public Optional<EventRequestDto> getByEventIdAndRequesterId(@PathVariable Long eventId, @PathVariable Long userId){
        return eventRequestService.getByEventIdAndRequesterId(eventId, userId);
    }
}
