package ru.practicum.client;

import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc.UserActionControllerBlockingStub;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

@Component
public class UserActionClient {

    private final UserActionControllerBlockingStub collectorClient;

    public UserActionClient(@GrpcClient("collector") UserActionControllerBlockingStub collectorClient) {
        this.collectorClient = collectorClient;
    }

    public void sendViewEvent(long userId, long eventId) {
        collectorClient.collectUserAction(getUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW));
    }

    public void sendRegistrationEvent(long userId, long eventId) {
        collectorClient.collectUserAction(getUserAction(userId, eventId, ActionTypeProto.ACTION_REGISTER));
    }

    public void sendLikeEvent(long userId, long eventId) {
        collectorClient.collectUserAction(getUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE));
    }

    private UserActionProto getUserAction(long userId, long eventId, ActionTypeProto actionType) {
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
