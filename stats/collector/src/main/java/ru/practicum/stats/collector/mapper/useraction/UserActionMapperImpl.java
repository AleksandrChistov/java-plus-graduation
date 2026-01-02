package ru.practicum.stats.collector.mapper.useraction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

@Component
@Slf4j
public class UserActionMapperImpl implements UserActionMapper {

    @Override
    public UserActionAvro map(UserActionProto action) {
        log.debug("Mapping {} to UserActionAvro", action);

        String avroActionTypeName = action.getActionType().name()
                .replace("ACTION_", "")
                .toUpperCase();

        try {
            ActionTypeAvro avroActionType = ActionTypeAvro.valueOf(avroActionTypeName);

            return UserActionAvro.newBuilder()
                    .setUserId(action.getUserId())
                    .setEventId(action.getEventId())
                    .setTimestamp(Instant.ofEpochSecond(action.getTimestamp().getSeconds(), action.getTimestamp().getNanos()))
                    .setActionType(avroActionType)
                    .build();
        } catch (IllegalArgumentException e) {
            log.error("Unknown action type: {}", avroActionTypeName);
            throw e;
        } catch (NullPointerException e) {
            log.error("Null action type");
            throw e;
        }
    }

}
