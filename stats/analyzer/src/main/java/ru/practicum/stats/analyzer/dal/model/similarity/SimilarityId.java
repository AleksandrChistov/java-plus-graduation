package ru.practicum.stats.analyzer.dal.model.similarity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SimilarityId implements Serializable {
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
