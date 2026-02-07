package ru.practicum.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.mapper.UserActionMapper;
import ru.practicum.model.UserAction;
import ru.practicum.repository.UserActionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionHandler {

    private final UserActionRepository repository;

    @Transactional
    public void handle(UserActionAvro action) {
        Long eventId = action.getEventId();
        Long userId = action.getUserId();
        double rating = switch (action.getActionType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };

        if (!repository.existsByUserIdAndEventId(userId, eventId)) {
            repository.save(UserActionMapper.mapToUserAction(action));
        } else {
            UserAction actionForUpdate = repository.findByUserIdAndEventId(userId, eventId);
            if (actionForUpdate.getRating() < rating) {
                actionForUpdate.setRating(rating);
                actionForUpdate.setTimestamp(action.getTimestamp());
                log.debug("Обновление данных действия пользователя {}", actionForUpdate);
            }
        }

    }
}
