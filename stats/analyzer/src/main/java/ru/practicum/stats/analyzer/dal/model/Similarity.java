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
class SimilarityId implements Serializable {
    @Column(name = "event1", nullable = false, updatable = false)
    private Long event1;

    @Column(name = "event2", nullable = false, updatable = false)
    private Long event2;

    public static SimilarityId of(long event1, long event2) {
        long min = Math.min(event1, event2);
        long max = Math.max(event1, event2);
        return new SimilarityId(min, max);
    }
}

@Entity
@Table(name = "similarities")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Similarity {
    @EmbeddedId
    private SimilarityId id;

    @Column(name = "similarity", nullable = false)
    private Double similarity;

    @Column(name = "action_ts", nullable = false, updatable = false)
    private LocalDateTime actionDateTime;

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
