package ru.practicum.exception;

public class UserNotExistException extends RuntimeException {
    public UserNotExistException(Long userId) {
        super("Пользователя с id " + userId + " е существует");
    }
}
