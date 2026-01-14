package ru.practicum.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.model.State;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /* все комментарии автора */
    List<Comment> findByCreatorIdAndState(Long creatorId, State state);

    /* все комментарии под событием */
    List<Comment> findByEventIdAndState(Long eventId, State state);

    Optional<Comment> findByIdAndState(Long id, State state);

    @Modifying
    @Query("UPDATE Comment c SET c.text = :text WHERE c.id = :id")
    int updateCommentText(@Param("text") String text,
                          @Param("id") Long id);

    @Modifying
    @Query("UPDATE Comment c SET c.state = :state WHERE c.id IN (:ids)")
    void incrementState(@Param("state") State state,
                        @Param("ids") List<Long> ids);

    @Query("SELECT c FROM Comment c WHERE c.id IN (:ids)")
    List<Comment> findByIdIn(@Param("ids") List<Long> ids);

    List<Comment> findAll(Specification<Comment> spec, Pageable pageable);
}
