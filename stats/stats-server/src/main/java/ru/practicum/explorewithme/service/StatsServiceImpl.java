package ru.practicum.explorewithme.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsDto;
import ru.practicum.StatsParams;
import ru.practicum.StatsView;
import ru.practicum.explorewithme.mapper.StatsMapper;
import ru.practicum.explorewithme.model.Stats;
import ru.practicum.explorewithme.repository.StatsRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final StatsMapper mapper;

    @Override
    @Transactional
    public void saveHit(StatsDto statsDto) {
        Stats stats = mapper.toStats(statsDto);
        statsRepository.save(stats);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatsView> getStats(StatsParams param) {
        List<StatsView> viewStats;
        if (param.getUnique()) {
            viewStats = statsRepository.findAllUniqueIpAndTimestampBetweenAndUriIn(
                    param.getStart(),
                    param.getEnd(),
                    param.getUris()
            );
        } else {
            viewStats = statsRepository.findAllByTimestampBetweenAndUriIn(
                    param.getStart(),
                    param.getEnd(),
                    param.getUris()
            );
        }
        return viewStats;
    }
}
