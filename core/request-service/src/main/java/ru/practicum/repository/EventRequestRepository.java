package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.event.request.Status;
import ru.practicum.model.EventRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRequestRepository extends JpaRepository<EventRequest, Long> {
    List<EventRequest> findAllByRequesterId(Long requesterId);

    Optional<EventRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    @Modifying
    @Query("""
            UPDATE EventRequest e
            SET e.status = :status
            WHERE e.id = :eventRequestId""")
    int updateStatus(Long eventRequestId, Status status);

    List<EventRequest> findAllByEventId(Long eventId);

    @Query("""
            SELECT e FROM EventRequest e
            WHERE e.id IN (:requestIds)
            """)
    List<EventRequest> findByRequestIds(List<Long> requestIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE EventRequest e
            SET e.status = :status
            WHERE e.id IN (:requestIds)
            """)
    int updateStatusForRequestsIds(List<Long> requestIds, Status status);

    @Query("SELECT e FROM EventRequest e WHERE e.id IN (:requestIds)")
    List<EventRequest> findByIdIn(List<Long> requestIds);
}

