package ru.practicum.comment.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentSearchParam;
import ru.practicum.dto.comment.CommentUpdateRequestDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.event.State;
import ru.practicum.dto.event.request.EventRequestDto;
import ru.practicum.dto.event.request.Status;
import ru.practicum.dto.user.UserDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.*;
import ru.practicum.feignClient.eventRequest.EventRequestClient;
import ru.practicum.feignClient.user.UserClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepo;
    private final UserClient userClient;
    private final EventRepository eventRepo;
    private final EventRequestClient eventRequestClient;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<CommentDto> getCommentsByUser(Long userId) {
        log.info("Получаем все комментарии пользователя id={}", userId);
        return commentRepo.findByCreatorIdAndState(userId, State.PUBLISHED).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getCommentsByEvent(Long eventId) {
        log.info("Получаем все комментарии для события id={}", eventId);
        return commentRepo.findByEventIdAndState(eventId, State.PUBLISHED).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto dto) {
        log.info("Начинаем создание комментария {} от пользователя id={} для события id={}", dto, userId, eventId);
        UserDto user = userClient.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));
        log.info("Определен пользователь комментатор {}", user);
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));
        log.info("Определено событие для комментария {}", event);

        EventRequestDto eventRequest = eventRequestClient.getByEventIdAndRequesterId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("EventRequest", eventId));

        if (!eventRequest.getStatus().equals(Status.CONFIRMED)) {
            log.error("Нельзя оставить комментарий: нет подтвержденной заявки на мероприятие id={} от пользователя id={}",
                    eventId, userId);
            throw new CreateCommentException(eventId, "заявка на мероприятие не была подтверждена");
        }
        Comment saved = commentRepo.save(CommentMapper.toEntity(dto, user, event));
        log.info("Создание комментария завершено {}", saved);
        return CommentMapper.toDto(saved);
    }

    @Transactional
    @Override
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto dto) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " not found"));
        log.info("Определен комментарий для обновления {}", comment);
        if (!comment.getCreatorId().equals(userId)) {
            log.error("Пользователь id={} не может обновить комментарий id={} так как не является его автором",
                    userId, commentId);
            throw new ForbiddenException("User not allowed to edit this comment");
        }

        int checkUpdate = commentRepo.updateCommentText(dto.getText(), commentId);
        if (checkUpdate < 1) {
            log.error("Обновление комментария id={} не было проведено", commentId);
            throw new InvalidUpdateException(commentId);
        }
        comment.setText(dto.getText());
        log.info("Обновление комментария id={} прошло успешно", commentId);
        return CommentMapper.toDto(comment);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public List<CommentDto> updateCommentState(CommentUpdateRequestDto request) {
        log.info("Начинаем обновление статусов на {} для комментариев id={}", request.getState(), request.getCommentIds());
        commentRepo.incrementState(State.valueOf(request.getState()), request.getCommentIds());
        entityManager.flush();
        List<Comment> updateComments = commentRepo.findByIdIn(request.getCommentIds());
        log.info("Обновление комментариев прошло успешно: {}", updateComments);
        return updateComments.stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    @Transactional
    @Override
    public void deleteComment(Long userId, Long commentId) {
        log.info("Начинаем удаление комментария id={} пользователем id={}", commentId, userId);
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " not found"));

        if (!comment.getCreatorId().equals(userId)) {
            log.error("Пользователь id={} не может удалить комментарий id={}", userId, commentId);
            throw new ForbiddenException("User not allowed to delete this comment");
        }
        log.info("Удаление комментария id={} прошло успешно", commentId);
        commentRepo.delete(comment);
    }

    @Transactional
    @Override
    public void deleteCommentByAdmin(Long commentId) {
        log.info("Начинаем удаление комментария id={} админом", commentId);
        if (!commentRepo.existsById(commentId)) {
            log.error("Комментарий id={} не найден", commentId);
            throw new NotFoundException("Comment with id=" + commentId + " not found");
        }
        log.info("Удаление комментария id={} прошло успешно", commentId);
        commentRepo.deleteById(commentId);
    }

    @Override
    public CommentDto getById(Long commentId) {
        log.info("Начинаем получени комментария по id={}", commentId);
        Comment comment = commentRepo.findByIdAndState(commentId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Комментарий", commentId));
        log.info("Получение комментария прошло успешно: {}", comment);
        return CommentMapper.toDto(comment);
    }

    @Override
    public List<CommentDto> getCommentWithParamAdmin(CommentSearchParam commentSearchParam, Pageable page) {
        log.info("Начинаем получение комментариев с фильтрацией для Админ API");
        Specification<Comment> spec = createSpecificationComment(commentSearchParam);
        List<Comment> comments = commentRepo.findAll(spec, page);
        log.info("Получение комментариев прошло успешно: {}", comments);
        return comments.stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    private Specification<Comment> createSpecificationComment(final CommentSearchParam searchParam) {
        LocalDateTime rangeStart = searchParam.getRangeStart();
        LocalDateTime rangeEnd = searchParam.getRangeEnd();
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            log.error("Конец выборки находится во временной шкале раньше начала");
            throw new InvalidRequestException("Время окончания выборки не может быть раньше времени начала");
        }
        return (root, query, cb) -> {
            log.info("Начинаем фильтрацию переданных параметров");
            List<Predicate> predicates = new ArrayList<>();

            log.info("Проводим фильтрацию по состояниям {}", searchParam.getStates());
            if (searchParam.getStates() != null && !searchParam.getStates().isEmpty()) {
                predicates.add(root.get("state").in(searchParam.getStates()));
            }

            log.info("Проводим фильтарацию по временным рамкам: {}, {}", rangeStart, rangeEnd);
            if (rangeStart != null && rangeEnd != null) {
                log.info("Фильтрация по времени публикации комментария, начальное время: {} конечное время {}",
                        rangeStart, rangeEnd);
                predicates.add(cb.between(root.get("created"), rangeStart, rangeEnd));
            } else {
                if (rangeStart != null) {
                    log.info("Время окончания выборки не указано, проводим фильтрацию по всем комментариям от {}", rangeStart);
                    predicates.add(cb.greaterThanOrEqualTo(root.get("created"), rangeStart));
                }

                if (rangeEnd != null) {
                    log.info("Время начала выборки не указано, проводим фильтрацию по всем комментариям до {}", rangeEnd);
                    predicates.add(cb.lessThanOrEqualTo(root.get("created"), rangeEnd));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

