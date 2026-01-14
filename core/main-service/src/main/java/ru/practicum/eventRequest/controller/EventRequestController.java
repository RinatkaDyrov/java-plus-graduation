package ru.practicum.eventRequest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.eventRequest.dto.EventRequestDto;
import ru.practicum.eventRequest.service.EventRequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class EventRequestController {
    private final EventRequestService eventRequestService;

    @GetMapping
    public List<EventRequestDto> getUsersEventList(@PathVariable Long userId) {
        log.info("Получение запросов на участие в событии пользователя с id {}", userId);
        return eventRequestService.getUsersRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventRequestDto createUserRequestToEvent(@PathVariable Long userId,
                                                    @RequestParam Long eventId) {
        log.info("Создание запроса на участие события с id: {} пользователем id: {}", eventId, userId);
        return eventRequestService.createRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public EventRequestDto cancelUserRequestToEvent(@PathVariable Long userId,
                                                    @PathVariable Long requestId) {
        log.info("Отмена запроса с id: {} пользователемс id: {}", requestId, userId);
        return eventRequestService.cancelRequest(userId, requestId);
    }

}
