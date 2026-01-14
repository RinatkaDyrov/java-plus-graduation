package ru.practicum.compilation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.compilation.model.Compilation;

import java.util.List;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @Query(value = """
                SELECT * FROM compilations
                WHERE (:pinned IS NULL OR pinned = :pinned)
                LIMIT :size OFFSET :from
            """, nativeQuery = true)
    List<Compilation> findAllWithFilter(@Param("pinned") Boolean pinned,
                                        @Param("from") int from,
                                        @Param("size") int size);
}
