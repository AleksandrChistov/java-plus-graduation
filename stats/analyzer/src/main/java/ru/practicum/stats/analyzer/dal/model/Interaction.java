package ru.practicum.stats.analyzer.dal.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
class InteractionId implements Serializable {
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false, updatable = false)
    private Long eventId;

    public static InteractionId of(long userId, long eventId) {
        return new InteractionId(userId, eventId);
    }
}

@Entity
@Table(name = "interactions")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Interaction {
    @EmbeddedId
    private InteractionId id;

    @Column(name = "rating", nullable = false)
    private Float rating;

    @Column(name = "action_ts", nullable = false, updatable = false)
    private LocalDateTime actionDateTime;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Interaction that = (Interaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
