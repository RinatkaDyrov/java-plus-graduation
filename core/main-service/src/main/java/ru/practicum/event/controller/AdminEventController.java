package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentSearchParam;
import ru.practicum.comment.dto.CommentUpdateRequestDto;
import ru.practicum.comment.service.CommentService;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventSearchParam;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.eventRequest.dto.UpdateEventRequest;
import ru.practicum.event.service.EventService;
import ru.practicum.helper.RequestParamHelper;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {
    private final EventService eventService;
    private final CommentService commentService;

    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0", required = false) @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10", required = false) @Positive Integer size) {
        Pageable page = PageRequest.of(from, size);
        log.info("Получаем события в Админ API с фильтрацией");
        EventSearchParam eventSearchParam = EventSearchParam.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .build();
        return eventService.getEventsWithParamAdmin(eventSearchParam, page);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventRequest request) {
        log.info("Публикация события в Админ API");
        return eventService.updateEventByAdmin(eventId, request);
    }

    @GetMapping("/comments")
    public List<CommentDto> getComments(@RequestParam(required = false) List<String> state,
                                        @RequestParam(required = false)
                                        @DateTimeFormat(pattern = RequestParamHelper.DATE_TIME_FORMAT) LocalDateTime rangeStart,
                                        @RequestParam(required = false)
                                        @DateTimeFormat(pattern = RequestParamHelper.DATE_TIME_FORMAT) LocalDateTime rangeEnd,
                                        @RequestParam(defaultValue = "0", required = false) @PositiveOrZero Integer from,
                                        @RequestParam(defaultValue = "10", required = false) @Positive Integer size) {
        Pageable page = PageRequest.of(from, size);
        log.info("Получаем комментарии в Админ API с фильтрацией");
        CommentSearchParam commentSearchParam = CommentSearchParam.builder()
                .states(state)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .build();
        return commentService.getCommentWithParamAdmin(commentSearchParam, page);
    }

    @PatchMapping("/comments/{commentId}")
    public List<CommentDto> updateCommentState(@Valid @RequestBody CommentUpdateRequestDto request) {
        log.info("Публикация комментариев в Админ API");
        return commentService.updateCommentState(request);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@PathVariable Long commentId) {
        log.info("Удаление комментария в Админ API");
        commentService.deleteCommentByAdmin(commentId);
    }
}
