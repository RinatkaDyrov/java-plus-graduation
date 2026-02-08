package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.grpc.stats.event.RecommendationsControllerGrpc;
import stats.messages.analyzer.AnalyzerMessages;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class RecommendationsClient {

    private final RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    public RecommendationsClient(@GrpcClient("ANALYZER") RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client) {
        this.client = client;
        log.info("### Клиент рекомендаций инициализирован с помощью gRPC: сервис analyzer");
    }

    public Stream<AnalyzerMessages.RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        log.debug("Получение рекомендаций пользователя (userId: {}), maxResults: {}", userId, maxResults);

        try {
            AnalyzerMessages.UserPredictionsRequestProto request = AnalyzerMessages.UserPredictionsRequestProto.newBuilder()
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            log.debug("Прием gRPC запроса: {}", request);
            Iterator<AnalyzerMessages.RecommendedEventProto> iterator = client.getRecommendationsForUser(request);

            List<AnalyzerMessages.RecommendedEventProto> results = new ArrayList<>();
            int count = 0;
            while (iterator.hasNext()) {
                AnalyzerMessages.RecommendedEventProto item = iterator.next();
                results.add(item);
                count++;
                log.trace("Отправка рекомендаций {}: {}", count, item);
            }

            log.debug("Отправка {} рекомендаций пользователю (userId: {})", count, userId);
            return results.stream();

        } catch (Exception e) {
            log.error("Ошибка при получении рекомендаций пользователя (userId: {}): {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to get recommendations for user: " + userId, e);
        }
    }

    public Stream<AnalyzerMessages.RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        log.debug("Получение списка схожих событий (eventId: {}, userId: {}, maxResults: {})", eventId, userId, maxResults);

        try {
            AnalyzerMessages.SimilarEventsRequestProto request = AnalyzerMessages.SimilarEventsRequestProto.newBuilder()
                    .setEventId(eventId)
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            log.debug("Отправка запроса на схожие события: {}", request);
            Iterator<AnalyzerMessages.RecommendedEventProto> iterator = client.getSimilarEvents(request);

            List<AnalyzerMessages.RecommendedEventProto> results = new ArrayList<>();
            int count = 0;
            while (iterator.hasNext()) {
                AnalyzerMessages.RecommendedEventProto item = iterator.next();
                results.add(item);
                count++;
                log.trace("Получение списка схожих событий: {}: {}", count, item);
            }

            log.debug("Отправка {} похожих событий по событию (eventId: {})", count, eventId);
            return results.stream();

        } catch (Exception e) {
            log.error("Ошибка при получении похожих событий (eventId {}: {}", eventId, e.getMessage(), e);
            throw new RuntimeException("Failed to get similar events for event: " + eventId, e);
        }
    }

    public Stream<AnalyzerMessages.RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        log.debug("Получение значения взаимодействий для событий: {}", eventIds);

        try {
            AnalyzerMessages.InteractionsCountRequestProto request = AnalyzerMessages.InteractionsCountRequestProto.newBuilder()
                    .addAllEventId(eventIds)
                    .build();

            log.debug("Отправка запроса на подсчет взаимодействий для {} events", eventIds.size());
            Iterator<AnalyzerMessages.RecommendedEventProto> iterator = client.getInteractionsCount(request);

            List<AnalyzerMessages.RecommendedEventProto> results = new ArrayList<>();
            int count = 0;
            while (iterator.hasNext()) {
                AnalyzerMessages.RecommendedEventProto item = iterator.next();
                results.add(item);
                count++;
                log.trace("Количество полученных взаимодействий {}: {}", count, item);
            }

            log.debug("Отправка {} взаимодействий с событиями", count);
            return results.stream();

        } catch (Exception e) {
            log.error("Ошибка при подсчете взаимодействий для событий {}: {}", eventIds, e.getMessage(), e);
            throw new RuntimeException("Failed to get interactions count for events", e);
        }
    }


    public boolean isServiceAvailable() {
        try {
            log.debug("Проверка доступности gRPC сервиса");
            client.getInteractionsCount(
                    AnalyzerMessages.InteractionsCountRequestProto.newBuilder()
                            .addEventId(0L) // тестовый ID
                            .build()
            ).hasNext();
            log.debug("gRPC сервис доступен");
            return true;
        } catch (Exception e) {
            log.warn("gRPC сервис недоступен: {}", e.getMessage());
            return false;
        }
    }

    private Stream<AnalyzerMessages.RecommendedEventProto> asStream(Iterator<AnalyzerMessages.RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
