package ru.practicum.user.repository;

import org.springframework.data.domain.Pageable;
import ru.practicum.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id >= :from")
    Page<User> findByIdAfter(Long from, Pageable pageable);

    List<User> findByIdIn(List<Long> ids);
}
