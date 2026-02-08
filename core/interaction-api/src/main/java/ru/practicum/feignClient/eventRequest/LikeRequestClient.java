package ru.practicum.feignClient.eventRequest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.event.request.EventRequestDto;

import java.util.Optional;

@FeignClient(name = "REQUEST-LIKE-SERVICE", fallback = LikeRequestClientFallback.class)
public interface LikeRequestClient {

    @GetMapping("/users/{userId}/requests/{eventId}/like/internal")
    Optional<EventRequestDto> getByEventIdAndRequesterId(@PathVariable Long eventId, @PathVariable Long userId);
}
