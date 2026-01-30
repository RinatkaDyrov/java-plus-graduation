package ru.practicum.feignClient.event;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import ru.practicum.dto.event.EventFullDto;

import java.util.Optional;

@FeignClient(name = "EVENT-SERVICE", fallback = EventClientFallback.class)
public interface EventClient {

    @GetMapping("/events/{eventId}/internal")
    Optional<EventFullDto> getEventById(@PathVariable Long eventId);

    @PutMapping("/events/{eventId}/{increment}/internal")
    Boolean incrementConfirmedRequests(@PathVariable("eventId") Long eventId,
                                       @PathVariable("increment") Integer increment);

//    @PutMapping("/{eventId}/{increment}/internal")
//    Boolean updateConfirmedRequests(@PathVariable Long eventId,
//                                    @PathVariable Integer increment);
}
