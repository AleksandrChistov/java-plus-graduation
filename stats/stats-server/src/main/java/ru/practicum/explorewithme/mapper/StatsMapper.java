package ru.practicum.explorewithme.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.StatsDto;
import ru.practicum.explorewithme.model.Stats;

@Mapper(componentModel = "spring")
public interface StatsMapper {

    @Mapping(target = "id", ignore = true)
    Stats toStats(StatsDto statsDto);

}
