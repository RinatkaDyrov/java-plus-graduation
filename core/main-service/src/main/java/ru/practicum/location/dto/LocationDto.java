package ru.practicum.location.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationDto {
    Float lat;
    Float lon;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationDto that = (LocationDto) o;
        return Objects.equals(lat, that.lat) && Objects.equals(lon, that.lon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lon);
    }
}
