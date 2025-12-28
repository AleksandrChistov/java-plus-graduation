package ru.practicum.explorewithme.comment.error.exception;

public class RuleViolationException extends RuntimeException {
    public RuleViolationException(String massage) {
        super(massage);
    }
}
