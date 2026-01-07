package ru.practicum.stats.analyzer.dal.model.interaction;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "interactions")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interaction {
    @EmbeddedId
    private InteractionId id;

    @Column(name = "rating", nullable = false)
    private Double rating;

    @Column(name = "action_ts", nullable = false, updatable = false)
    private Instant actionDateTime;

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
