package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.StatClient;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.service.CommentService;
import ru.practicum.dto.RequestHitDto;
import ru.practicum.event.dto.*;
import ru.practicum.eventRequest.service.EventRequestService;
import ru.practicum.event.service.EventService;
import ru.practicum.eventRequest.dto.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {
    private final EventService eventService;
    private final StatClient statClient;
    private final EventRequestService eventRequestService;
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId,
                                 @RequestBody @Valid NewEventRequest request) {
        log.info("Сохранение мероприятия");
        return eventService.addEvent(userId, request);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable @Positive Long userId,
                                    @PathVariable @Positive Long eventId,
                                    @Valid @RequestBody UpdateEventRequest request) {
        log.info(String.format("Обновление события с id %s пользователем с id %d", eventId, userId));
        return eventService.updateEventByUser(userId, eventId, request);
    }

    @GetMapping
    public List<EventShortDto> getEventsByUser(@PathVariable("userId") long userId,
                                               @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
                                               @RequestParam(required = false, defaultValue = "10") @Positive Integer size,
                                               HttpServletRequest request) {
        Pageable page = PageRequest.of(from, size);
        RequestHitDto hitDto = RequestHitDto.builder()
                .app("ewm-main-service")
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        log.info("Отправляем данные по запросу getEventsByUser в сервис статистики {}", hitDto.toString());
        statClient.sendHit(hitDto);
        return eventService.getUsersEvents(userId, page, request.getRemoteAddr());
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable("userId") long userId,
                                     @PathVariable("eventId") long eventId,
                                     HttpServletRequest request) {
        log.info("Получение конкретной информации для конкретного пользователя о мероприятии");
        RequestHitDto hitDto = RequestHitDto.builder()
                .app("ewm-main-service")
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        log.info("Отправляем данные по запросу getEventById в сервис статистики {}", hitDto.toString());
        statClient.sendHit(hitDto);
        return eventService.getByIdPrivate(userId, eventId, request.getRemoteAddr());
    }

    @GetMapping("/{eventId}/requests")
    public List<EventRequestDto> getRequestByEvent(@PathVariable("userId") long userId,
                                                   @PathVariable("eventId") long eventId) {
        log.info("Получение информации о заявке на участие в Event id={} от пользователя id={}", eventId, userId);
        return eventRequestService.getAllByEventId(userId, eventId);
    }


    @PatchMapping("/{eventId}/requests")
    public EventRequestUpdateResult updateRequestStatus(@PathVariable Long userId,
                                                        @PathVariable Long eventId,
                                                        @RequestBody EventRequestUpdateDto request) {
        log.info("___Начинаем обработку запроса обновления {}", request);
        return eventRequestService.updateRequestState(userId, eventId, request);
    }

    @GetMapping("/comments")
    public List<CommentDto> getUserComments(@PathVariable Long userId) {
        log.info("Получение всех комментариев пользователя id = {}", userId);
        return commentService.getCommentsByUser(userId);
    }

    @GetMapping("/comments/{eventId}")
    public List<CommentDto> getEventComments(@PathVariable Long userId,
                                             @PathVariable Long eventId) {
        log.info("Получение всех комментариев для мероприятия id = {}", eventId);
        return commentService.getCommentsByEvent(eventId);
    }

//    @GetMapping("/comments/{commentId}")
//    public CommentDto getById(@PathVariable Long userId,
//                              @PathVariable Long commentId) {
//        log.info("Получение комментария по id={}", commentId);
//        return commentService.getById(commentId);
//    }

    @PostMapping("/comments/{eventId}")
    public CommentDto addComment(@PathVariable Long userId,
                                 @PathVariable Long eventId,
                                 @Valid @RequestBody NewCommentDto dto) {
        log.info("Добавление комментария пользователем id={} для мероприятия id={}", userId, eventId);
        return commentService.createComment(userId, eventId, dto);
    }

    @PatchMapping("/comments/{commentId}")
    public CommentDto editComment(@PathVariable Long userId,
                                  @PathVariable Long commentId,
                                  @Valid @RequestBody NewCommentDto dto) {
        log.info("Обновление комментария id={} пользователем id={}", commentId, userId);
        return commentService.updateComment(userId, commentId, dto);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        log.info("Удаление комментария id={} пользователем id={}", commentId, userId);
        commentService.deleteComment(userId, commentId);
    }
}
