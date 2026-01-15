package ru.practicum.ewm.stats.avro.exception;

public class DeserializationException extends RuntimeException {
    public DeserializationException(String message, Throwable cause) {
        super(message,cause);
    }
}
