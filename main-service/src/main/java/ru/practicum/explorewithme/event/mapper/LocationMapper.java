package ru.practicum.explorewithme.event.mapper;

import org.mapstruct.Mapper;
import ru.practicum.explorewithme.event.dto.LocationDto;
import ru.practicum.explorewithme.event.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    Location toEntity(LocationDto dto);

    LocationDto toDto(Location location);

}
