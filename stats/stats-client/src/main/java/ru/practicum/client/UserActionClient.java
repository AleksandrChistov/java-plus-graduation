package ru.practicum.client;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

@Component
public class UserActionClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collectorClient;

    public Empty sendViewEvent(long userId, long eventId) {
        return collectorClient.collectUserAction(getUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW));
    }

    public Empty sendRegistrationEvent(long userId, long eventId) {
        return collectorClient.collectUserAction(getUserAction(userId, eventId, ActionTypeProto.ACTION_REGISTER));
    }

    public Empty sendLikeEvent(long userId, long eventId) {
        return collectorClient.collectUserAction(getUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE));
    }

    public UserActionProto getUserAction(long userId, long eventId, ActionTypeProto actionType) {
        return UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(actionType)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano()))
                .build();
    }

}
