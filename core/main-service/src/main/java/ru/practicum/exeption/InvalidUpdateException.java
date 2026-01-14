package ru.practicum.exeption;

import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidUpdateException extends OptimisticLockException {
    public InvalidUpdateException(String message) {
        super(message);
    }

    public InvalidUpdateException(Long id) {
        super(String.format("Комментарий с id %d не был обновлен", id));
    }
}
