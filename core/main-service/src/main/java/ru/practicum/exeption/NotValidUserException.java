package ru.practicum.exeption;

public class NotValidUserException extends RuntimeException {
    public NotValidUserException(Long userId) {
        super("Только владелец запроса может отменить заявку на участие, пользователь с id " + userId + " не владелец");
    }
}
