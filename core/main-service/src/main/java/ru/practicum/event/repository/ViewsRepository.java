package ru.practicum.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.model.Views;

public interface ViewsRepository extends JpaRepository<Views, Long> {

    @Modifying
    @Query(value = "INSERT INTO views (event_id, ip) " +
            "SELECT :eventId, :ip " +
            "WHERE NOT EXISTS (" +
            "   SELECT 1 " +
            "   FROM views " +
            "   WHERE event_id = :eventId " +
            "   AND ip = :ip" +
            ")",
            nativeQuery = true)
    void upsertNative(
            @Param("eventId") Long eventId,
            @Param("ip") String ip
    );

    @Query("SELECT COUNT(v) FROM Views v WHERE v.eventId = :eventId")
    Integer countByEventId(@Param("eventId") Long eventId);
}
