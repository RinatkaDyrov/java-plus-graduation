package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.StatClient;
import ru.practicum.dto.RequestHitDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventSearchParam;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService eventService;
    private final StatClient statClient;

    @GetMapping("/{eventId}")
    public EventFullDto getById(@PathVariable Long eventId, HttpServletRequest request) {
        log.info("Получаем мероприятие для Public API по id = {}", eventId);
        RequestHitDto hitDto = RequestHitDto.builder()
                .app("ewm-main-service")
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        log.info("Отправляем данные по запросу getById в сервис статистики {}", hitDto.toString());
        statClient.sendHit(hitDto);
        return eventService.getByIdPublic(eventId, request.getRemoteAddr());
    }

    @GetMapping
    public List<EventShortDto> getEventsWithParam(@RequestParam(required = false) String text,
                                                  @RequestParam(required = false) List<Long> categories,
                                                  @RequestParam(required = false) Boolean paid,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                  @RequestParam(defaultValue = "false")
                                                  Boolean onlyAvailable,
                                                  @RequestParam(required = false) String sort,
                                                  @RequestParam(defaultValue = "0", required = false)
                                                  @PositiveOrZero Integer from,
                                                  @RequestParam(defaultValue = "10", required = false) @Positive Integer size,
                                                  HttpServletRequest request) {
        log.info("Получаем мероприятия с фильтрацией");
        Pageable page = PageRequest.of(from, size);
        EventSearchParam eventSearchParam = EventSearchParam.builder()
                .text(text)
                .states(List.of("PUBLISHED"))
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .build();
        RequestHitDto hitDto = RequestHitDto.builder()
                .app("ewm-main-service")
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        log.info("Отправляем данные по запросу getEventsWithParam в сервис статистики {}", hitDto.toString());
        statClient.sendHit(hitDto);
        return eventService.getEventsWithParamPublic(eventSearchParam, page, request.getRemoteAddr());
    }
}
