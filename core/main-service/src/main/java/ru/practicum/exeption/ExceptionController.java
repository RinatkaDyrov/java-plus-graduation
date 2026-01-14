package ru.practicum.exeption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice
@Slf4j
public class ExceptionController {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionDto emailMustBeUniqueExceptionHandler(EmailMustBeUniqueException e) {
        log.error("Обрабатываем исключение EmailMustBeUniqueException");
        return ExceptionDto.builder()
                .status(HttpStatus.CONFLICT.toString())
                .reason("email not unique")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto invalidRequestExceptionHandler(InvalidRequestException e) {
        log.error("Обрабатываем исключение InvalidRequestException");
        return ExceptionDto.builder()
                .status(HttpStatus.BAD_REQUEST.toString())
                .reason("request is invalid")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto invalidDbRequestExceptionHandler(DataIntegrityViolationException e) {
        log.error("Обрабатываем исключение DataIntegrityViolationException");
        return ExceptionDto.builder()
                .status(HttpStatus.BAD_REQUEST.toString())
                .reason("request is invalid")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionDto userNotExistExceptionHandler(UserNotExistException e) {
        log.error("Обрабатываем исключение UserNotExistException");
        return ExceptionDto.builder()
                .status(HttpStatus.NOT_FOUND.toString())
                .reason("user not found")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionDto notValidUserExceptionHandler(NotValidUserException e) {
        log.error("Обрабатываем исключение NotValidUserException");
        return ExceptionDto.builder()
                .status(HttpStatus.FORBIDDEN.toString())
                .reason("Только владлец заявки может ее отменять")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler
    public ExceptionDto handleNotFound(final NotFoundException e) {
        log.error("Обрабатываем исключение NotFoundException");
        return ExceptionDto.builder()
                .status(HttpStatus.NOT_FOUND.toString())
                .reason("resources not found")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionDto handlerValidEventDay(final MethodArgumentNotValidException e) {
        log.error("Обрабатываем исключение MethodArgumentNotValidException");
        return ExceptionDto.builder()
                .status(HttpStatus.CONFLICT.toString())
                .reason("for the requested operation the conditions are not met")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionDto handlerConflict(final ConflictException e) {
        log.error("Обрабатываем исключение ConflictException");
        return ExceptionDto.builder()
                .status(HttpStatus.CONFLICT.toString())
                .reason("for the requested operation the conditions are not met.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionDto handleRequestModeration(final RequestModerationException e) {
        log.error("Обрабатываем исключение RequestModerationException");
        return ExceptionDto.builder()
                .status(HttpStatus.CONFLICT.toString())
                .reason("For the requested operation the conditions are not met.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionDto handleUpdateComment(final InvalidUpdateException e) {
        log.error("Обрабатываем исключение InvalidUpdateException");
        return ExceptionDto.builder()
                .status(HttpStatus.CONFLICT.toString())
                .reason("For the requested operation the conditions are not met.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionDto handleForbiddenComment(final ForbiddenException e) {
        log.error("Обрабатываем исключение ForbiddenException");
        return ExceptionDto.builder()
                .status(HttpStatus.FORBIDDEN.toString())
                .reason("For the requested operation the conditions are not met.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionDto handleCreateComment(final CreateCommentException e) {
        log.error("Обрабатываем исключение CreateCommentException");
        return ExceptionDto.builder()
                .status(HttpStatus.CONFLICT.toString())
                .reason("For the requested operation the conditions are not met.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }
}