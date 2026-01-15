package ru.practicum.stats.collector.mapper.useraction;

import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionProto;

public interface UserActionMapper {

    UserActionAvro map(UserActionProto action);

}
