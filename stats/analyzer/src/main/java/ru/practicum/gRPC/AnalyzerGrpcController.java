package ru.practicum.gRPC;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.grpc.stats.event.RecommendationsControllerGrpc;
import ru.practicum.service.RecommendationServiceImpl;
import stats.messages.analyzer.AnalyzerMessages;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AnalyzerGrpcController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationServiceImpl recommendationService;

    @Override
    public void getRecommendationsForUser (AnalyzerMessages.UserPredictionsRequestProto request,
                                          StreamObserver<AnalyzerMessages.RecommendedEventProto> responseObserver) {
        log.info("Получен gRPC-запрос: GetRecommendationsForUser user_id={} max_results={}",
                request.getUserId(), request.getMaxResults());
        recommendationService.getRecommendations(request).forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public void getSimilarEvents(AnalyzerMessages.SimilarEventsRequestProto request, StreamObserver<AnalyzerMessages.RecommendedEventProto> responseObserver) {
        log.info("Получен gRPC-запрос: GetSimilarEvents event_id={} user_id={} max_results={}",
                request.getEventId(), request.getUserId(), request.getMaxResults());
        recommendationService.getSimilarEvents(request).forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public void getInteractionsCount(AnalyzerMessages.InteractionsCountRequestProto request, StreamObserver<AnalyzerMessages.RecommendedEventProto> responseObserver) {
        log.info("Получен gRPC-запрос: GetInteractionsCount event_ids={}", request.getEventIdList());
        recommendationService.getInteractionsCount(request).forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }
}
