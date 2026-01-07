package ru.practicum.stats.analyzer.dal.model.interaction;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class InteractionId implements Serializable {
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false, updatable = false)
    private Long eventId;

    public static InteractionId of(long userId, long eventId) {
        return new InteractionId(userId, eventId);
    }
}
