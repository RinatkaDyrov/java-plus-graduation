package ru.practicum.exception;

public class EmailMustBeUniqueException extends RuntimeException {
    public EmailMustBeUniqueException(String email) {
        super("пользователь с email " + email + " уже существует");
    }
}
