package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.RequestHitDto;
import ru.practicum.dto.ResponseDto;
import ru.practicum.model.HitEntity;
import ru.practicum.model.StatEntity;


@Component
public class HitMapper {
    public HitEntity toEntity(RequestHitDto dto) {
        HitEntity entity = new HitEntity();
        entity.setApp(dto.getApp());
        entity.setUri(dto.getUri());
        entity.setIp(dto.getIp());
        entity.setTimestamp(dto.getTimestamp());
        return entity;
    }

    public ResponseDto toResponseDto(StatEntity stat) {
        return new ResponseDto(stat.getApp(), stat.getUri(), stat.getHits());
    }
}

