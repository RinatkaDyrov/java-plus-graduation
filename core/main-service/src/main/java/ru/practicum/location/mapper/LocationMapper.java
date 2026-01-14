package ru.practicum.location.mapper;

import ru.practicum.location.dto.LocationDto;
import ru.practicum.location.dto.NewLocationRequest;
import ru.practicum.location.model.Location;

public class LocationMapper {
    public static LocationDto mapToDto(Location location) {
        return LocationDto.builder()
                .lon(location.getLon())
                .lat(location.getLat())
                .build();
    }

    public static Location mapToLocationNew(NewLocationRequest request) {
        return Location.builder()
                .lat(request.getLat())
                .lon(request.getLon())
                .build();
    }
}
