package ru.practicum.eventRequest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.eventRequest.model.EventRequest;
import ru.practicum.eventRequest.model.Status;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRequestRepository extends JpaRepository<EventRequest, Long> {
    List<EventRequest> findAllByRequester_Id(Long requesterId);

    Optional<EventRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    @Modifying
    @Query("UPDATE EventRequest e SET e.status = :status WHERE e.id = :eventRequestId")
    int updateStatus(@Param("eventRequestId") Long eventRequestId, @Param("status") Status status);

    List<EventRequest> findAllByEventId(Long eventId);

    @Query("SELECT e FROM EventRequest e " +
            "WHERE e.id IN (:requestIds) ")
    List<EventRequest> findByRequestIds(@Param("requestIds") List<Long> requestIds);

    @Modifying
    @Query("UPDATE EventRequest e " +
            "SET e.status = :status " +
            "WHERE e.id IN (:requestIds)")
    int updateStatusForRequestsIds(@Param("requestIds") List<Long> requestIds,
                                   @Param("status") Status status);

    @Query("SELECT e FROM EventRequest e WHERE e.id IN (:requestIds)")
    List<EventRequest> findByIdIn(@Param("requestIds") List<Long> requestIds);
}

