package ru.practicum.mapper;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.model.EventSimilarity;

public class EventSimilarityMapper {

    public static EventSimilarity mapToEventSimilarity(EventSimilarityAvro avro) {

        return EventSimilarity.builder()
                .event1(avro.getEventA())
                .event2(avro.getEventB())
                .similarity(avro.getScore())
                .timestamp(avro.getTimestamp())
                .build();
    }
}
