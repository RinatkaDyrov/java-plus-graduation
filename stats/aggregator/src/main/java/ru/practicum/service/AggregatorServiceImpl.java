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
        double actionWeight = 0.0;
        switch (actionAvro.getActionType()) {
            case VIEW -> actionWeight = VIEW_WEIGHT;
            case REGISTER -> actionWeight = REGISTER_WEIGHT;
            case LIKE -> actionWeight = LIKE_WEIGHT;
        }
        Double currentWeight = usersWeight.getOrDefault(actionAvro.getUserId(), 0.0);

        if (actionWeight <= currentWeight) {
            return List.of();
        }
        usersWeight.put(actionAvro.getUserId(), actionWeight);
        ownWeightSum.put(actionAvro.getEventId(), ownWeightSum.getOrDefault(actionAvro.getEventId(), 0.0) +
                actionWeight - currentWeight);

        List<EventSimilarityAvro> result = new ArrayList<>();
        Instant ts = Instant.now();

        for (Long key : actionWeightMap.keySet()) {
            if (key == actionAvro.getEventId()) {
                continue;
            }
            if (actionWeightMap.get(key).containsKey(actionAvro.getUserId())) {
                double minWeightSum = get(actionAvro.getEventId(), key);
                double event2Weight = actionWeightMap.get(key).get(actionAvro.getUserId());

                minWeightSum += Math.min(event2Weight, actionWeight) - Math.min(event2Weight, currentWeight);

                double s1 = ownWeightSum.get(actionAvro.getEventId());
                double s2 = ownWeightSum.get(key);

                long eventA = Math.min(actionAvro.getEventId(), key);
                long eventB = Math.max(actionAvro.getEventId(), key);
                double score = getSimilarityCoefficient(s1, s2, minWeightSum);

                EventSimilarityAvro eventSimilarityAvro = EventSimilarityAvro.newBuilder()
                        .setEventA(eventA)
                        .setEventB(eventB)
                        .setScore(score)
                        .setTimestamp(ts)
                        .build();

                result.add(eventSimilarityAvro);
                put(actionAvro.getEventId(), key, minWeightSum);
            }
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
