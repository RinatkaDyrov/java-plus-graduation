package ru.practicum.location.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewLocationRequest {
    @NotNull
    Float lat;

    @NotNull
    Float lon;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewLocationRequest that = (NewLocationRequest) o;
        return Objects.equals(lat, that.lat) && Objects.equals(lon, that.lon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lon);
    }
}
