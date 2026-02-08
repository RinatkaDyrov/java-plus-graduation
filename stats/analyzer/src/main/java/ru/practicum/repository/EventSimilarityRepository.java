package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.EventSimilarity;

import java.util.List;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    boolean existsByEvent1AndEvent2(Long event1, Long event2);

    EventSimilarity findByEvent1AndEvent2(Long event1, Long event2);

    @Query("""
            SELECT e FROM EventSimilarity e
            WHERE (e.event1 IN :eventIds AND e.event2 NOT IN :eventIds)
               OR (e.event2 IN :eventIds AND e.event1 NOT IN :eventIds)
            """)
    List<EventSimilarity> findSimilarPairsForEvents(List<Long> eventIds);

    List<EventSimilarity> findByEvent1OrEvent2(Long event1, Long event2);
}