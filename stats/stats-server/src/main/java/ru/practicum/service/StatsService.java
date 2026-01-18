package ru.practicum.service;

import ru.practicum.dto.RequestHitDto;
import ru.practicum.dto.ResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    List<ResponseDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);

    void saveHit(RequestHitDto hitDto);

}
