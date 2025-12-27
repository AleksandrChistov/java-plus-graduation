package ru.practicum.explorewithme.shared.error.exception;

public class RuleViolationException extends RuntimeException {
    public RuleViolationException(String massage) {
        super(massage);
    }
}
