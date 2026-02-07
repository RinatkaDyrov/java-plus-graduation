package ru.practicum.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.mapper.EventSimilarityMapper;
import ru.practicum.model.EventSimilarity;
import ru.practicum.repository.EventSimilarityRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSimilarityHandler {

    private final EventSimilarityRepository repository;

    @Transactional
    public void handle(EventSimilarityAvro avro) {
        Long eventA = avro.getEventA();
        Long eventB = avro.getEventB();

        if (!repository.existsByEvent1AndEvent2(eventA, eventB)) {
            repository.save(EventSimilarityMapper.mapToEventSimilarity(avro));
            log.debug("Сохранение данных схожести событий {}", avro);
        } else {
            EventSimilarity existing = repository.findByEvent1AndEvent2(eventA, eventB);
            existing.setSimilarity(avro.getScore());
            existing.setTimestamp(avro.getTimestamp());
            log.debug("Обновление данных схожести событий {}", existing);
        }
    }
}