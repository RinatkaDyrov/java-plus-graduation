package ru.practicum.exeption;

public class UserNotExistException extends RuntimeException {
    public UserNotExistException(Long userId) {
        super("Пользователя с id " + userId + " е существует");
    }
}
