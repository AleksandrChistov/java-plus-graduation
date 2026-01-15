package ru.practicum.stats.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.stats.collector.config.KafkaConfig;
import ru.practicum.stats.collector.kafka.KafkaUserActionProducer;
import ru.practicum.stats.collector.mapper.useraction.UserActionMapper;

import java.util.List;

@GrpcService
@Slf4j
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final List<String> topics;

    private final KafkaUserActionProducer userActionProducer;

    private final UserActionMapper userActionMapper;

    public UserActionController(KafkaConfig kafkaConfig, KafkaUserActionProducer userActionProducer, UserActionMapper userActionMapper) {
        this.topics = kafkaConfig.getUserActionsTopics();
        this.userActionProducer = userActionProducer;
        this.userActionMapper = userActionMapper;
    }

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Получен UserAction JSON: {}", request.toString());

            UserActionAvro userActionAvro = userActionMapper.map(request);

            log.info("Отправка UserAction Avro в Kafka: {}", userActionAvro.toString());

            topics.forEach(topic -> userActionProducer
                    .send(topic, userActionAvro.getTimestamp().toEpochMilli(), userActionAvro.getEventId(), userActionAvro)
            );

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при отправке UserAction в Kafka", e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e.getCause())
            ));
        }
    }

}
