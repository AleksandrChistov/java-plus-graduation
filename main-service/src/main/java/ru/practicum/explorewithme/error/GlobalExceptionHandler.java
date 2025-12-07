package ru.practicum.explorewithme.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.explorewithme.error.exception.BadRequestException;
import ru.practicum.explorewithme.error.exception.NotFoundException;
import ru.practicum.explorewithme.error.exception.RuleViolationException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(final DataIntegrityViolationException ex) {
        log.warn("409 Conflict: {}", ex.getMessage());
        String stackTrace = getStackTrace(ex);
        String timestamp = getCurrentTimestamp();
        return getResponseEntity(HttpStatus.CONFLICT, ex.getMessage(), "Data conflict", timestamp, stackTrace);
    }

    @ExceptionHandler(RuleViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiError> handleRuleViolation(final RuleViolationException ex) {
        log.warn("409 Conflict: {}", ex.getMessage());
        String stackTrace = getStackTrace(ex);
        String timestamp = getCurrentTimestamp();
        return getResponseEntity(HttpStatus.CONFLICT, ex.getMessage(), "For the requested operation the conditions are not met.", timestamp, stackTrace);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(final HttpMessageNotReadableException ex) {
        log.warn("400 {}", ex.getMessage());
        String stackTrace = getStackTrace(ex);
        String timestamp = getCurrentTimestamp();
        return getResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), "Invalid JSON", timestamp, stackTrace);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleMissingServletRequestParameter(final MissingServletRequestParameterException ex) {
        log.warn("400 {}", ex.getMessage());
        String stackTrace = getStackTrace(ex);
        String timestamp = getCurrentTimestamp();
        return getResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), "Missing parameter", timestamp, stackTrace);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleInvalidArgument(final MethodArgumentNotValidException ex) {
        log.warn("400 {}", ex.getMessage());

        String message = Optional
                .ofNullable(ex.getBindingResult().getAllErrors().getFirst())
                .map(ObjectError::getDefaultMessage)
                .orElse(ex.getMessage());

        String stackTrace = getStackTrace(ex);
        String timestamp = getCurrentTimestamp();
        return getResponseEntity(HttpStatus.BAD_REQUEST, message, "Invalid argument", timestamp, stackTrace);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleArgumentTypeMismatch(final MethodArgumentTypeMismatchException ex) {
        log.warn("400 {}", ex.getMessage());
        String stackTrace = getStackTrace(ex);
        String timestamp = getCurrentTimestamp();
        return getResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), "Argument type mismatch", timestamp, stackTrace);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleBadRequest(final BadRequestException ex) {
        log.warn("400 {}", ex.getMessage());
        String stackTrace = getStackTrace(ex);
        String timestamp = getCurrentTimestamp();
        return getResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), "Bad request", timestamp, stackTrace);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleInvalidInput(final ConstraintViolationException ex) {
        log.warn("400 {}", ex.getMessage());

        String message = ex.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse(ex.getMessage());

        String stackTrace = getStackTrace(ex);
        String timestamp = getCurrentTimestamp();
        return getResponseEntity(HttpStatus.BAD_REQUEST, message, "Invalid input data", timestamp, stackTrace);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiError> handleNotFound(final NotFoundException ex) {
        log.warn("404 {}", ex.getMessage());
        String stackTrace = getStackTrace(ex);
        String timestamp = getCurrentTimestamp();
        return getResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage(), "The required object was not found.", timestamp, stackTrace);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiError> handleException(final Exception ex) {
        log.error("500 {}", ex.getMessage(), ex);
        String stackTrace = getStackTrace(ex);
        String timestamp = getCurrentTimestamp();
        return getResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), "Internal server error", timestamp, stackTrace);
    }

    private ResponseEntity<ApiError> getResponseEntity(HttpStatus httpStatus, String reason, String message, String timestamp, String stackTrace) {
        ApiError apiError = new ApiError(httpStatus, reason, message, timestamp, stackTrace);
        return new ResponseEntity<>(apiError, httpStatus);
    }

    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(formatter);
    }
}
