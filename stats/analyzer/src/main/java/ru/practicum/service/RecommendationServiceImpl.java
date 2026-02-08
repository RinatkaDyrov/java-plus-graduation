package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.model.EventSimilarity;
import ru.practicum.model.UserAction;
import ru.practicum.repository.EventRatingSumView;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.repository.UserActionRepository;
import stats.messages.analyzer.AnalyzerMessages;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final UserActionRepository userActionRepository;
    private final EventSimilarityRepository eventSimilarityRepository;

    @Override
    public List<AnalyzerMessages.RecommendedEventProto> getRecommendations(AnalyzerMessages.UserPredictionsRequestProto request) {

        long userId = request.getUserId();
        int maxResults = (int) request.getMaxResults();

        List<Long> userEventIds = userActionRepository.findEventIdsOrderByMaxMarkDesc(
                userId, PageRequest.of(0, maxResults)
        );
        if (userEventIds.isEmpty()) {
            return List.of();
        }

        List<EventSimilarity> similarPairs = eventSimilarityRepository.findSimilarPairsForEvents(userEventIds);

        Map<Long, Double> userRatings = userActionRepository.findAllByUserId(userId).stream()
                .collect(Collectors.toMap(UserAction::getEventId, UserAction::getRating));

        Map<Long, Double> predictedScores = new HashMap<>();
        Map<Long, Double> similaritySums = new HashMap<>();

        for (EventSimilarity pair : similarPairs) {
            Set<Long> known = new HashSet<>(userEventIds);

            boolean e1Known = known.contains(pair.getEvent1());
            boolean e2Known = known.contains(pair.getEvent2());
            if (e1Known == e2Known) continue;

            long knownEvent = e1Known ? pair.getEvent1() : pair.getEvent2();
            long candidate  = e1Known ? pair.getEvent2() : pair.getEvent1();
            double similarity = pair.getSimilarity();

            Double userRating = userRatings.get(knownEvent);

            if (userRating == null) {
                continue;
            }

            predictedScores.merge(candidate, similarity * userRating, Double::sum);
            similaritySums.merge(candidate, similarity, Double::sum);
        }

        return predictedScores.entrySet().stream()
                .filter(e -> similaritySums.getOrDefault(e.getKey(), 0.0) > 0)
                .map(e -> AnalyzerMessages.RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey())
                        .setScore(e.getValue() / similaritySums.get(e.getKey()))
                        .build())
                .sorted(Comparator.comparingDouble(AnalyzerMessages.RecommendedEventProto::getScore).reversed())
                .limit(maxResults)
                .toList();
    }

    @Override
    public List<AnalyzerMessages.RecommendedEventProto> getSimilarEvents(AnalyzerMessages.SimilarEventsRequestProto request) {
        long userId = request.getUserId();
        long eventId = request.getEventId();
        int maxResults = (int) request.getMaxResults();

        Set<Long> interactions = new HashSet<>(userActionRepository.findEventIdsByUserId(userId));
        List<EventSimilarity> similarPairs = eventSimilarityRepository.findByEvent1OrEvent2(eventId, eventId);

        return similarPairs.stream()
                .map(pair -> {
                    long candidate = (pair.getEvent1() == eventId) ? pair.getEvent2() : pair.getEvent1();
                    return Map.entry(candidate, pair.getSimilarity());
                })
                .filter(e -> !interactions.contains(e.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(e -> AnalyzerMessages.RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey())
                        .setScore(e.getValue())
                        .build())
                .toList();
    }

    @Override
    public List<AnalyzerMessages.RecommendedEventProto> getInteractionsCount(AnalyzerMessages.InteractionsCountRequestProto request) {
        List<Long> eventIds = request.getEventIdList();
        List<EventRatingSumView> results = userActionRepository.sumRatingsForEvents(eventIds);

        return results.stream()
                .map(r -> AnalyzerMessages.RecommendedEventProto.newBuilder()
                        .setEventId(r.getEventId())
                        .setScore(r.getRatingSum())
                        .build())
                .toList();
    }
}