package ru.practicum.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RequestModerationException extends RuntimeException {
    public RequestModerationException(String message) {
        super(message);
    }

    public RequestModerationException(Long id, String description) {
        super(String.format("Невозможно оставить заявку на событие с id=%d:  %s", id, description));
    }
}
