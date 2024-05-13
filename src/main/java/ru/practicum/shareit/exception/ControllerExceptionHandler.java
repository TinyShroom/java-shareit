package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ValidationException;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorDto exceptionHandler(ValidationException e) {
        log.info("ValidationException: {}", e.getMessage());
        return new ErrorDto(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorDto exceptionHandler(MethodArgumentNotValidException e) {
        log.info("MethodArgumentNotValidException: {}", e.getMessage());
        return new ErrorDto("Validation exception");
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler
    public ErrorDto exceptionHandler(NotFoundException e) {
        log.info("NotFoundException: {}", e.getMessage());
        return new ErrorDto(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorDto exceptionHandler(ConstraintException e) {
        log.info("DatabaseConstraintException: {}", e.getMessage());
        return new ErrorDto(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler
    public ErrorDto exceptionHandler(DataIntegrityViolationException e) {
        log.info("DataIntegrityViolationException: {}", e.getMessage());
        return new ErrorDto(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorDto exceptionHandler(AccessDeniedException e) {
        log.info("AccessDeniedException: {}", e.getMessage());
        return new ErrorDto(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorDto exceptionHandler(IllegalArgumentException e) {
        log.info("IllegalArgumentException: {}", e.getMessage());
        return new ErrorDto(e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public ErrorDto exceptionHandler(Exception e) {
        log.error("Exception: {}", e.getMessage(), e);
        return new ErrorDto(e.getMessage());
    }
}