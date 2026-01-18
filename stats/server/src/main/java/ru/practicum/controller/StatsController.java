package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.RequestHitDto;
import ru.practicum.dto.ResponseDto;
import ru.practicum.service.StatsService;


import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveHit(@RequestBody RequestHitDto requestHitDto) {
        log.info("POST /hit: {}", requestHitDto);
        statsService.saveHit(requestHitDto);
    }

    @GetMapping("/stats")
    public List<ResponseDto> getStats(@RequestParam(required = false)
                                      @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                      @RequestParam(required = false) List<String> uris,
                                      @RequestParam(defaultValue = "false") boolean unique) {

        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end must be specified");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end must be after start");
        }
        log.info("GET /stats: start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        return statsService.getStats(start, end, uris, unique);
    }
}