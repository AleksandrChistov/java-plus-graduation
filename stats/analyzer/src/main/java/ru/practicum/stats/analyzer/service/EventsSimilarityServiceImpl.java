package ru.practicum.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.analyzer.dal.dao.SimilarityRepository;
import ru.practicum.stats.analyzer.dal.model.similarity.Similarity;
import ru.practicum.stats.analyzer.dal.model.similarity.SimilarityId;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventsSimilarityServiceImpl implements EventsSimilarityService {

    private final SimilarityRepository similarityRepository;

    @Override
    public Similarity save(EventSimilarityAvro event) {
        SimilarityId id = SimilarityId.of(event.getEventA(), event.getEventB());

        Similarity similarity = Similarity.builder()
                .id(id)
                .similarity(event.getScore())
                .actionTimestamp(event.getTimestamp())
                .build();

        log.info("Сохраняем похожесть событий {}", similarity);

        return similarityRepository.save(similarity);
    }

}
