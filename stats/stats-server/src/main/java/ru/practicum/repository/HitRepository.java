package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.model.HitEntity;
import ru.practicum.model.StatEntity;


import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HitRepository extends JpaRepository<HitEntity, Long> {

    // Статистика по всем
    @Query("""
            SELECT new ru.practicum.model.StatEntity(e.app, e.uri, COUNT(e))
            FROM HitEntity e
            WHERE e.timestamp BETWEEN :start AND :end
            GROUP BY e.app, e.uri
            ORDER BY COUNT(e) DESC
            """)
    List<StatEntity> getAllStats(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);

    @Query("""
            SELECT new ru.practicum.model.StatEntity(e.app, e.uri, COUNT(DISTINCT e.ip))
            FROM HitEntity e
            WHERE e.timestamp BETWEEN :start AND :end
            GROUP BY e.app, e.uri
            ORDER BY COUNT(DISTINCT e.ip) DESC
            """)
    List<StatEntity> getUniqueStats(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    // Статистика по конкретным URI
    @Query("""
            SELECT new ru.practicum.model.StatEntity(e.app, e.uri, COUNT(e))
            FROM HitEntity e
            WHERE e.timestamp BETWEEN :start AND :end AND e.uri IN :uris
            GROUP BY e.app, e.uri
            ORDER BY COUNT(e) DESC
            """)
    List<StatEntity> getAllStatsByUris(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       @Param("uris") List<String> uris);

    @Query("""
            SELECT new ru.practicum.model.StatEntity(e.app, e.uri, COUNT(DISTINCT e.ip))
            FROM HitEntity e
            WHERE e.timestamp BETWEEN :start AND :end AND e.uri IN :uris
            GROUP BY e.app, e.uri
            ORDER BY COUNT(DISTINCT e.ip) DESC
            """)
    List<StatEntity> getUniqueStatsByUris(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end,
                                          @Param("uris") List<String> uris);

}


