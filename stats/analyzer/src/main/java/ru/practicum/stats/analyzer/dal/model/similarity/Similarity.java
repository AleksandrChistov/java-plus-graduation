package ru.practicum.stats.analyzer.dal.model.similarity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "similarities")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Similarity {
    @EmbeddedId
    private SimilarityId id;

    @Column(name = "similarity", nullable = false)
    private Double similarity;

    @Column(name = "action_ts", nullable = false, updatable = false)
    private Instant actionTimestamp;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Similarity that = (Similarity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
