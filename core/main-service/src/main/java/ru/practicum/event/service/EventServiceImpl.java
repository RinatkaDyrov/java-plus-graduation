package ru.practicum.event.service;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.model.StateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.ViewsRepository;
import ru.practicum.eventRequest.dto.NewEventRequest;
import ru.practicum.eventRequest.dto.UpdateEventRequest;
import ru.practicum.exeption.ConflictException;
import ru.practicum.exeption.InvalidRequestException;
import ru.practicum.exeption.NotFoundException;
import ru.practicum.location.mapper.LocationMapper;
import ru.practicum.location.model.Location;
import ru.practicum.location.repository.LocationRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor(onConstructor_ = @Autowired)
public class EventServiceImpl implements EventService {
    EventRepository eventRepository;
    LocationRepository locationRepository;
    UserRepository userRepository;
    CategoryRepository categoryRepository;
    ViewsRepository viewsRepository;

    @Transactional
    @Override
    public EventFullDto addEvent(Long userId, NewEventRequest request) {
        log.info("Начинаем создание мероприятия {} пользователем id = {}", request, userId);
        User initiator = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", userId));
        log.info("Получаем пользователя создателя мероприятия {}", initiator);
        Category category = categoryRepository.findById(request.getCategory())
                .orElseThrow(() -> new NotFoundException("Category", request.getCategory()));
        log.info("Получаем категорию для меропрития {}", category);
        Location location = locationRepository.save(LocationMapper.mapToLocationNew(request.getLocation()));
        log.info("Создаем локацию меропрития {}", location);
        Event create = EventMapper.mapToEventNew(request, category, location, initiator);
        Event event = eventRepository.save(create);
        log.info("Создание меропрития {} завершено", event);
        return EventMapper.mapToFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventRequest request) {
        Event event = getEvent(eventId);
        log.info("Валидация события (id {}) для обновления пользователем (id {})", event.getId(), userId);
        if (!(userRepository.existsById(userId) &&
                event.getInitiator().getId().equals(userId) &&
                !event.getState().equals(State.PUBLISHED))) {
            log.warn("Конфликт при запросе на обновление события");
            throw new ConflictException("Данное событие нельзя обновлять");
        }
        updateEventFields(event, request);

        return EventMapper.mapToFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventRequest request) {
        Event event = getEvent(eventId);
        log.info("Валидация события (id {}) для обновления", event.getId());
        if (request.getStateAction() != null
                && request.getStateAction().equals(StateAction.PUBLISH_EVENT.toString())
                && !event.getState().equals(State.PENDING)) {
            log.warn("Попытка публикации события, которое не в ожидании публикации");
            throw new ConflictException("Событие можно публиковать, только если оно в состоянии ожидания публикации");
        }
        if (request.getStateAction() != null
                && request.getStateAction().equals(StateAction.REJECT_EVENT.toString())
                && event.getState().equals(State.PUBLISHED)) {
            log.warn("Попытка отклонения события, которое уже опубликовано");
            throw new ConflictException("Событие можно отклонить, только если оно еще не опубликовано ");
        }
        updateEventFields(event, request);
        return EventMapper.mapToFullDto(eventRepository.save(event));
    }

    @Transactional
    @Override
    public EventFullDto getByIdPrivate(Long userId, Long eventId, String ip) {
        log.info("Начинаем получение мероприятия id = {}", eventId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event", eventId));
        updateViews(event.getId(), ip);
        log.info("Мероприятие успешно получено: {}", event);
        return EventMapper.mapToFullDto(event);
    }

    @Transactional
    @Override
    public EventFullDto getByIdPublic(Long eventId, String ip) {
        log.info("Начинаем поиск мероприятия id = {} со статусом Published", eventId);
        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event", eventId));
        log.info("Мероприятие найдено: {}", event);
        updateViews(eventId, ip);
        return EventMapper.mapToFullDto(event);
    }

    @Transactional
    @Override
    public List<EventShortDto> getUsersEvents(Long userId, Pageable page, String ip) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", userId));
        Page<Event> events = eventRepository.findByInitiatorId(userId, page);
        events.forEach(event -> updateViews(event.getId(), ip));
        log.info("Получаем все опубликованные мероприятия для пользователя id = {}: размер списка: {}, " +
                "список меропритий: {}", userId, events.getSize(), events.getContent());
        return events.stream()
                .sequential()
                .map(EventMapper::mapToShortDto)
                .toList();
    }

    @Transactional
    @Override
    public List<EventShortDto> getEventsWithParamAdmin(EventSearchParam eventSearchParam, Pageable page) {
        log.info("Начинаем получение событий с фильтрами для Admin API");
        Specification<Event> spec = createSpecification(eventSearchParam);
        List<Event> events = eventRepository.findAll(spec, page);
        if (events.isEmpty()) {
            log.info("Не найдено событий для поиска Admin API с фильтрами");
        }
        log.info("Возвращаем список мероприятий для Admin API: {}", events);
        return events.stream()
                .map(EventMapper::mapToShortDto)
                .toList();
    }

    @Transactional
    @Override
    public List<EventShortDto> getEventsWithParamPublic(EventSearchParam eventSearchParam, Pageable page, String ip) {
        log.info("Начинаем получение событий с фильтрами для Public API");
        Specification<Event> spec = createSpecification(eventSearchParam);
        List<EventShortDto> events = eventRepository.findAll(spec, page).stream()
                .map(EventMapper::mapToShortDto)
                .toList();
        events.forEach(event -> updateViews(event.getId(), ip));
        log.info("Возвращаем список мероприятий для Public API: {}", events);
        return events;
    }

    private Event getEvent(Long id) {
        log.info("Поиск мероприятия (id {})", id);
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие", id));
    }

    private void updateEventFields(Event event, UpdateEventRequest request) {
        if (request.getAnnotation() != null) {
            log.debug("Обновление краткого описания события");
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            Long id = request.getCategory().longValue();
            event.setCategory(categoryRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Категория", id)));
            log.debug("Обновление категории события");
        }
        if (request.getDescription() != null) {
            log.debug("Обновление полного описания");
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                log.warn("Попытка публикации события c датой, не соответствующей требованиям: {}", request.getEventDate());
                throw new InvalidRequestException("Событие не может начинаться раньше, чем через 2 часа");
            }
            log.debug("Обновление даты и времени события");
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) {
            log.debug("Обновление места проведения события");
            Location newLocation = locationRepository.save(LocationMapper.mapToLocationNew(request.getLocation()));
            event.setLocation(newLocation);
        }
        if (request.getPaid() != null) {
            log.debug("Обновление поля необходимости оплаты события");
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            if (request.getParticipantLimit() < 0) {
                throw new InvalidRequestException("Лимит участников не может быть отрицательным");
            }
            log.debug("Обновление лимита участников события");
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            log.debug("Обновление статуса пре-модерации для события");
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getStateAction() != null) {
            log.debug("Обновление статуса события");
            switch (request.getStateAction()) {
                case "SEND_TO_REVIEW":
                    event.setState(State.PENDING);
                    break;
                case "PUBLISH_EVENT":
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                default:
                    event.setState(State.CANCELED);
            }
        }
        if (request.getTitle() != null) {
            log.debug("Обновление заголовка события");
            event.setTitle(request.getTitle());
        }
    }

    private void updateViews(Long eventId, String ip) {
        log.info("Сохраняем ip для Event id = {}", eventId);
        viewsRepository.upsertNative(eventId, ip);
        log.info("Обновляем счетчик просмотров для мероприятия id = {}", eventId);
        Integer eventViews = viewsRepository.countByEventId(eventId);
        if (eventViews > 0) {
            eventRepository.updateViews(eventId, eventViews);
        }
    }

    private Specification<Event> createSpecification(final EventSearchParam searchParam) {
        LocalDateTime rangeStart = searchParam.getRangeStart();
        LocalDateTime rangeEnd = searchParam.getRangeEnd();
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            log.error("Конец выборки находится во временной шкале раньше начала");
            throw new InvalidRequestException("Время окончания выборки не может быть раньше времени начала");
        }
        return (root, query, cb) -> {
            log.info("Начинаем фильтрацию переданных параметров");
            List<Predicate> predicates = new ArrayList<>();
            List<String> predicateLogs = new ArrayList<>();

            log.info("Проводим фильтрацию по пользователям {}", searchParam.getUsers());
            if (searchParam.getUsers() != null && !searchParam.getUsers().isEmpty() &&
                    searchParam.getUsers().getFirst() != 0) {
                predicateLogs.add("Пользователи: " + searchParam.getUsers());
                predicates.add(root.get("initiator").get("id").in(searchParam.getUsers()));
            }

            log.info("Проводим фильтрацию по состояниям {}", searchParam.getStates());
            if (searchParam.getStates() != null && !searchParam.getStates().isEmpty()) {
                predicateLogs.add("Статусы: " + searchParam.getStates());
                predicates.add(root.get("state").in(searchParam.getStates()));
            }

            log.info("Проводим фильтрацию по категориям {}", searchParam.getCategories());
            if (searchParam.getCategories() != null && !searchParam.getCategories().isEmpty() &&
                    searchParam.getCategories().getFirst() != 0) {
                predicateLogs.add("Категории: " + searchParam.getCategories());
                predicates.add(root.get("category").get("id").in(searchParam.getCategories()));
            }

            log.info("Проводим фильтарацию по временным рамкам: {}, {}", rangeStart, rangeEnd);
            if (rangeStart != null && rangeEnd != null) {
                log.info("Фильтрация по времени начала мероприятия, начальное время: {} конечное время {}",
                        rangeStart, rangeEnd);
                predicateLogs.add("Временной диапазон: от " + rangeStart + " до " + rangeEnd);
                predicates.add(cb.between(root.get("eventDate"), rangeStart, rangeEnd));
            } else {
                if (rangeStart != null) {
                    log.info("Время окончания выборки не указано, проводим фильтрацию по всем событиям от {}", rangeStart);
                    predicateLogs.add("Время начала выборки: " + rangeStart);
                    predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
                }

                if (rangeEnd != null) {
                    log.info("Время начала выборки не указано, проводим фильтрацию по всем событиям до {}", rangeEnd);
                    predicateLogs.add("Время окончания выборки: " + rangeEnd);
                    predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
                }

                if (rangeStart == null && rangeEnd == null) {
                    log.info("Диапазон не указан, выборка по всем предстоящим событиям");
                    predicateLogs.add("Время выборки не указано, выборка по предстоящим событиям от: " + LocalDateTime.now());
                    predicates.add(cb.greaterThan(root.get("eventDate"), cb.currentTimestamp()));
                }
            }

            log.info("Проводим текстовый поиск по запросу {}", searchParam.getText());
            if (searchParam.getText() != null) {
                String searchText = searchParam.getText().toLowerCase();

                if (searchText.isEmpty() || searchText.isBlank()) {
                    log.info("Текстовый поиск не выполняется - строка пуста");
                } else if ("0".equals(searchText)) {
                    log.info("Обнаружено значение '0' в текстовом поиске");
                } else {
                    log.info("Создание предиката для поиска по текстовому запросу в аннотации");
                    Predicate annotationPredicate = cb.like(
                            cb.lower(root.get("annotation")),
                            "%" + searchText + "%"
                    );

                    log.info("Создание предиката для поиска по текстовому запросу в описании");
                    Predicate descriptionPredicate = cb.like(
                            cb.lower(root.get("description")),
                            "%" + searchText + "%"
                    );
                    predicateLogs.add("Поиск по текстовому запросу: " + searchParam.getText());
                    log.info("Объединяем предикаты для поиска в обоих полях");
                    predicates.add(cb.or(annotationPredicate, descriptionPredicate));
                }
            }

            log.info("Добавляем сортировку по параметру {}", searchParam.getSort());
            if (searchParam.getSort() != null) {
                switch (searchParam.getSort().toUpperCase()) {
                    case "EVENT_DATE":
                        predicateLogs.add("Поиск с сортировкой по: EVENT_DATE");
                        query.orderBy(cb.asc(root.get("eventDate")));
                        break;
                    case "VIEWS":
                        predicateLogs.add("Поиск с сортировкой по: VIEWS");
                        query.orderBy(cb.asc(root.get("views")));
                        break;
                }
            }

            log.info("Проводим поиск по paid {}", searchParam.getPaid());
            if (searchParam.getPaid() != null) {
                predicateLogs.add("Поиск по paid с флагом: " + searchParam.getPaid());
                predicates.add(cb.lessThanOrEqualTo(root.get("paid"), searchParam.getPaid()));
            }

            log.info("Проводим поиск по onlyAvailable {}", searchParam.getOnlyAvailable());
            if (searchParam.getOnlyAvailable() != null && searchParam.getOnlyAvailable()) {
                Expression<Integer> limitExpr = root.get("participantLimit");
                Expression<Integer> requestsExpr = root.get("confirmedRequests");
                Predicate limitNotZero = cb.notEqual(limitExpr, 0);
                if (limitNotZero != null) {
                    predicateLogs.add("Поиск при participantLimit больше 0 и onlyAvailable с флагом: "
                            + searchParam.getOnlyAvailable());
                    predicates.add(cb.lessThan(requestsExpr, limitExpr));
                }
            }

            log.info("Количество собранных предикатов {}", predicates.size());
            log.info("Возвращаем лист с параметрами поиска: {}", predicateLogs);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
