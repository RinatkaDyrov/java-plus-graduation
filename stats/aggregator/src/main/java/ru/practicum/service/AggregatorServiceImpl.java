package ru.practicum.service;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AggregatorServiceImpl implements AggregatorService {

    private static final Double VIEW_WEIGHT = 0.4;
    private static final Double REGISTER_WEIGHT = 0.8;
    private static final Double LIKE_WEIGHT = 1.0;

    private final Map<Long, Map<Long, Double>> actionWeightMap = new HashMap<>();
    private final Map<Long, Double> ownWeightSum = new HashMap<>();
    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    @Override
    public List<EventSimilarityAvro> aggregate(SpecificRecordBase recordBase) {
        if (!(recordBase instanceof UserActionAvro actionAvro)) {
            throw new IllegalArgumentException(
                    "Некорректный формат записи: " + recordBase.getClass().getName()
            );
        }

        Map<Long, Double> usersWeight = actionWeightMap.computeIfAbsent(
                actionAvro.getEventId(),
                k -> new HashMap<>()
        );

        double actionWeight = switch (actionAvro.getActionType()) {
            case VIEW -> VIEW_WEIGHT;
            case REGISTER -> REGISTER_WEIGHT;
            case LIKE -> LIKE_WEIGHT;
        };

        double currentWeight = usersWeight.getOrDefault(actionAvro.getUserId(), 0.0);
        if (actionWeight <= currentWeight) {
            return List.of();
        }

        usersWeight.put(actionAvro.getUserId(), actionWeight);
        ownWeightSum.put(
                actionAvro.getUserId(),
                ownWeightSum.getOrDefault(actionAvro.getUserId(), 0.0) + actionWeight - currentWeight
        );

        Instant ts = Instant.now();
        List<EventSimilarityAvro> result = new ArrayList<>();

        for (Long key : actionWeightMap.keySet()) {
            if (key == actionAvro.getEventId()) {
                continue;
            }

            Map<Long, Double> usersOfOtherEvent = actionWeightMap.get(key);
            Double weightInOtherEvent = usersOfOtherEvent.get(actionAvro.getUserId());
            if (weightInOtherEvent == null) {
                continue;
            }

            double event2Weight = weightInOtherEvent;
            double minWeightSum = get(actionAvro.getEventId(), key);
            minWeightSum += Math.min(event2Weight, actionWeight) - Math.min(event2Weight, currentWeight);

            double s1 = ownWeightSum.get(actionAvro.getEventId());
            double s2 = ownWeightSum.get(key);

            long eventA = Math.min(actionAvro.getEventId(), key);
            long eventB = Math.max(actionAvro.getEventId(), key);

            double score = getSimilarityCoefficient(s1, s2, minWeightSum);

            EventSimilarityAvro out = EventSimilarityAvro.newBuilder()
                    .setEventA(eventA)
                    .setEventB(eventB)
                    .setScore(score)
                    .setTimestamp(ts)
                    .build();
            result.add(out);
            put(actionAvro.getEventId(), key, minWeightSum);
        }

        return result;
    }

    private double getSimilarityCoefficient(double s1, double s2, double minWeightSum) {
        if (s1 == 0.0 || s2 == 0.0) {
            return 0.0;
        }
        return minWeightSum / (Math.sqrt(s1) * Math.sqrt(s2));
    }

    public void put(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    public double get(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }
}
