package ru.practicum.client;

import ru.practicum.StatsDto;
import ru.practicum.StatsParams;
import ru.practicum.StatsView;

import java.util.List;

public interface StatsClient {

    void hit(StatsDto statsDto);

    List<StatsView> getStats(StatsParams statsParams);

}
