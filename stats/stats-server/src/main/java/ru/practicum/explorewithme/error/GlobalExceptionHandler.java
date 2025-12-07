package ru.practicum.explorewithme.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.explorewithme.error.exception.NotFoundException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiError> handleDataIntegrityViolationException(final DataIntegrityViolationException ex) {
        log.warn("409 Conflict: {}", ex.getMessage());
        return getResponseEntity(HttpStatus.CONFLICT, "Data conflict", ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleInvalidInput(MethodArgumentNotValidException ex) {
        log.warn("400 {}", ex.getMessage());

        String message = Optional
                .ofNullable(ex.getBindingResult().getAllErrors().getFirst())
                .map(ObjectError::getDefaultMessage)
                .orElse(ex.getMessage());

        String stackTrace = getStackTrace(ex);
        return getResponseEntity(HttpStatus.BAD_REQUEST, "Invalid input", message, stackTrace);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException ex) {
        log.warn("404 {}", ex.getMessage());
        String stackTrace = getStackTrace(ex);
        return new ApiError(HttpStatus.NOT_FOUND, "The required object was not found", ex.getMessage(), stackTrace);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiError> handleException(final Exception ex) {
        log.error("500 {}", ex.getMessage(), ex);
        String stackTrace = getStackTrace(ex);
        return getResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", ex.getMessage(), stackTrace);
    }

    private ResponseEntity<ApiError> getResponseEntity(HttpStatus httpStatus, String reason, String message, String stackTrace) {
        ApiError apiError = new ApiError(httpStatus, reason, message, stackTrace);
        return new ResponseEntity<>(apiError, httpStatus);
    }

    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}
