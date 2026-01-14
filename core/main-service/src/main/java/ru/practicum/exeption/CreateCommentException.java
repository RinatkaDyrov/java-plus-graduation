package ru.practicum.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CreateCommentException extends RuntimeException {
    public CreateCommentException(String message) {
        super(message);
    }

    public CreateCommentException(Long id, String description) {
        super(String.format("Невозможно оставить комментарий для события с id=%d:  %s", id, description));
    }
}
