package ru.practicum.explorewithme.service;

import ru.practicum.StatsDto;
import ru.practicum.StatsParams;
import ru.practicum.StatsView;

import java.util.List;

public interface StatsService {
    void saveHit(StatsDto statsDto);

    List<StatsView> getStats(StatsParams param);
}

