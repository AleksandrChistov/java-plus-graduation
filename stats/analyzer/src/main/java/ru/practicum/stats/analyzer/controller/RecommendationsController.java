package ru.practicum.stats.analyzer.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.*;
import ru.practicum.stats.analyzer.service.RecommendationsService;

import java.util.List;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationsService recommendationsService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Получение рекомендованных {} мероприятий для пользователя {}", request.getMaxResults(), request.getUserId());

            List<RecommendedEventProto> recommendations = recommendationsService.getRecommendationsForUser(request);

            log.info("Получены рекомендованные мероприятия для пользователя {}, {}", request.getUserId(), recommendations);

            for (RecommendedEventProto recommendation : recommendations) {
                responseObserver.onNext(recommendation);
            }

            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при генерации рекомендованных мероприятий для пользователя {}", request.getUserId(), e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e.getCause())
            ));
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Получение максимально похожих мероприятий на {}, с которыми не взаимодействовал пользователь {}", request.getEventId(), request.getUserId());

            List<RecommendedEventProto> similarEvents = recommendationsService.getSimilarEvents(request);

            log.info("Получены максимально похожие мероприятия на {}, с которыми не взаимодействовал пользователь {}, {}", request.getEventId(), request.getUserId(), similarEvents);

            for (RecommendedEventProto similarEvent : similarEvents) {
                responseObserver.onNext(similarEvent);
            }

            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при генерации максимально похожих мероприятий на {}, с которыми не взаимодействовал пользователь {}", request.getEventId(), request.getUserId(), e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e.getCause())
            ));
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Получение суммы весов для каждого мероприятия {}", request.getEventIdList());

            List<RecommendedEventProto> interactionsCounts = recommendationsService.getInteractionsCounts(request);

            log.info("Получены суммы весов для каждого мероприятия {}", interactionsCounts);

            for (RecommendedEventProto interactionsCount : interactionsCounts) {
                responseObserver.onNext(interactionsCount);
            }

            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при генерации сумм весов для каждого мероприятия {}", request.getEventIdList(), e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e.getCause())
            ));
        }
    }
}
