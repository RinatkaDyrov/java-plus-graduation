package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.UserAction;

import java.util.List;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    UserAction findByUserIdAndEventId(Long userId, Long eventId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    @Query("""
            SELECT ua.eventId
            FROM UserAction ua
            WHERE ua.userId = :userId
            GROUP BY ua.eventId
            ORDER BY MAX(ua.timestamp) DESC
            """)
    List<Long> findEventIdsOrderByMaxMarkDesc(Long userId, Pageable pageable);

    @Query("""
            SELECT ua.eventId
            FROM UserAction ua
            WHERE ua.userId = :userId
            """)
    List<Long> findEventIdsByUserId(Long userId);

    @Query("""
            SELECT ua.eventId as eventId, SUM(ua.rating) as ratingSum
            FROM UserAction ua
            WHERE ua.eventId IN :eventIds
            GROUP BY ua.eventId
            """)
    List<EventRatingSumView> sumRatingsForEvents(List<Long> eventIds);

    List<UserAction> findAllByUserId(Long userId);
}
