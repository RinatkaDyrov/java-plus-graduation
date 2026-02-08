package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.UserActionClient;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.State;
import ru.practicum.dto.event.request.EventRequestDto;
import ru.practicum.dto.event.request.EventRequestUpdateDto;
import ru.practicum.dto.event.request.EventRequestUpdateResult;
import ru.practicum.dto.event.request.Status;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.NotValidUserException;
import ru.practicum.exception.RequestModerationException;
import ru.practicum.feignClient.event.EventClient;
import ru.practicum.feignClient.user.UserClient;
import ru.practicum.mapper.EventRequestMapper;
import ru.practicum.model.EventRequest;
import ru.practicum.repository.EventRequestRepository;
import stats.messages.collector.UserAction;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventRequestServiceImpl implements EventRequestService {
    private final UserClient userClient;
    private final EventClient eventClient;
    private final EventRequestRepository eventRequestRepository;
    private final TransactionTemplate transactionTemplate;
    private final UserActionClient userActionClient;

    @Transactional(readOnly = true)
    @Override
    public List<EventRequestDto> getUsersRequests(Long userId) {
        userClient.getUserById(userId).orElseThrow(() -> new NotFoundException("EventRequest", userId));

        return eventRequestRepository.findAllByRequesterId(userId)
                .stream()
                .map(EventRequestMapper::mapToEventRequestDto).toList();
    }

    @Override
    public EventRequestDto createRequest(Long userId, Long eventId) {
        log.info("Начинаем создание заявки на участие в мероприятии id = {} от пользователя id = {}", eventId, userId);

        UserDto user = userClient.getUserById(userId).orElseThrow(() -> new NotFoundException("EventRequest", userId));
        EventFullDto event = eventClient.getEventById(eventId)
                .orElseThrow(() -> {
                            log.error("Вот тут то мы и упали потому что eventId={}", eventId);
                            return new NotFoundException("EventRequest", eventId);
                        }
                );

        if (eventRequestRepository.findByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new RequestModerationException(eventId, "Заявка уже была отправлена");
        }

        if (event.getParticipantLimit() > 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            log.error("Заявка не была добавлена: лимит заявок исчерпан: лимит={}, принятых заявок={}",
                    event.getParticipantLimit(), event.getConfirmedRequests());
            throw new RequestModerationException(eventId, "Лимит заявок исчерпан");
        }

        if (event.getInitiator().getId().equals(userId)) {
            log.error("Заявка не была отправлена: нельзя отправить заявку на собственное мероприятие");
            throw new RequestModerationException(eventId, "Нельзя отправить заявку на собственное мероприятие");
        }

        if (!event.getState().equals(State.PUBLISHED)) {
            log.error("Заявка не была отправлена: нельзя отправить заявку на неопубликованное мероприятие: {}",
                    event.getState());
            throw new RequestModerationException(eventId, "Нельзя отправить заявку на неопубликованное мероприятие");
        }

        boolean autoConfirm = (event.getParticipantLimit() == 0) || Boolean.FALSE.equals(event.getRequestModeration());
        Status requestStatus = autoConfirm ? Status.CONFIRMED : Status.PENDING;

        EventRequest saved = transactionTemplate.execute(tx -> {
            EventRequest newRequest = EventRequest.builder()
                    .created(LocalDateTime.now())
                    .requesterId(user.getId())
                    .eventId(event.getId())
                    .status(requestStatus)
                    .build();

            return eventRequestRepository.save(newRequest);
        });

        if (saved != null && saved.getStatus() == Status.CONFIRMED) {
            Boolean ok = eventClient.incrementConfirmedRequests(saved.getEventId(), 1);
            if (ok == null || !ok) {
                throw new RequestModerationException("Не удалось обновить confirmedRequests");
            }
        }
        userActionClient.collectUserAction(eventId, userId, UserAction.ActionTypeProto.ACTION_REGISTER, Instant.now());

        return EventRequestMapper.mapToEventRequestDto(saved);
    }

    @Override
    public EventRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отменяем заявку id={} пользователем id={}", requestId, userId);

        EventRequestDto result = transactionTemplate.execute(tx -> {
            EventRequest eventRequest = eventRequestRepository.findById(requestId)
                    .orElseThrow(() -> new NotFoundException("EventRequest", requestId));
            if (!eventRequest.getRequesterId().equals(userId)) {
                throw new NotValidUserException(userId);
            }
            Status previousStatus = eventRequest.getStatus();
            int updated = eventRequestRepository.updateStatus(requestId, Status.CANCELED);
            if (updated != 1) {
                throw new ConflictException("Не удалось отменить заявку");
            }
            eventRequest.setStatus(Status.CANCELED);
            if (previousStatus == Status.CONFIRMED) {
                Boolean ok = eventClient.incrementConfirmedRequests(eventRequest.getEventId(), -1);
                if (ok == null || !ok) {
                    throw new RequestModerationException("Не удалось обновить confirmedRequests");
                }
            }
            return EventRequestMapper.mapToEventRequestDto(eventRequest);
        });

        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventRequestDto> getAllByEventId(Long userId, Long eventId) {
        log.info("Поиск заявок на участие от пользователя id={} для Event id={}", userId, eventId);

        userClient.getUserById(userId).orElseThrow(() -> new NotFoundException("EventRequest", userId));
        eventClient.getEventById(eventId).orElseThrow(() -> new NotFoundException("EventRequest", eventId));

        return eventRequestRepository.findAllByEventId(eventId)
                .stream()
                .map(EventRequestMapper::mapToEventRequestDto)
                .toList();
    }

    @Override
    public EventRequestUpdateResult updateRequestState(Long userId, Long eventId, EventRequestUpdateDto updateDto) {
        log.info("Начинаем обновление заявок для событий id={} пользователем id={}", eventId, userId);

        String status = updateDto.getStatus();
        List<Long> requestIds = updateDto.getRequestIds();
        UserDto user = userClient.getUserById(userId).orElseThrow(() -> new NotFoundException("User", userId));

        log.info("Определен инициатор события {}", user);

        EventFullDto event = eventClient.getEventById(eventId)
                .orElseThrow(() -> new NotFoundException("Event", eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            log.error("Пользователь id={} не является инициатором события id={}", userId, eventId);
            throw new RequestModerationException("У пользователя нет доступа к данному событию");
        }

        List<EventRequest> requests = eventRequestRepository.findByRequestIds(requestIds);

        if (requests.stream().anyMatch(eventRequest -> !eventRequest.getStatus().equals(Status.PENDING))) {
            log.error("В списке есть заявка не находящаяся в статусе ожидания");
            throw new RequestModerationException(eventId, "Можно принимать заявки только в статусе ожидания");
        }

        int limit = event.getParticipantLimit();
        int confirmed = event.getConfirmedRequests();

        log.info("Проверяем наличие свободных мест");

        if (limit > 0 && limit <= confirmed) {
            log.error("Лимит заявок для мероприятия id={} уже исчерпан {}", eventId, limit - confirmed);
            throw new RequestModerationException(eventId, "Лимит заявок исчерпан");
        }

        int available = (limit == 0) ? Integer.MAX_VALUE : (limit - confirmed);

        List<Long> toConfirm;
        List<Long> toReject;

        if (status.equalsIgnoreCase("rejected")) {
            toConfirm = List.of();
            toReject = requestIds;
        } else {
            if (requestIds.size() > available) {
                toConfirm = requestIds.subList(0, available);
                toReject = requestIds.subList(available, requestIds.size());
            } else {
                toReject = List.of();
                toConfirm = requestIds;
            }
        }

        EventRequestUpdateResult result = transactionTemplate.execute(tx -> {
            EventRequestUpdateResult r = EventRequestUpdateResult.builder().build();

            if (!toConfirm.isEmpty()) {
                updateStatusAllRequest(toConfirm, Status.CONFIRMED);
                r.setConfirmedRequests(findAllByListIds(toConfirm));
            }
            if (!toReject.isEmpty()) {
                updateStatusAllRequest(toReject, Status.REJECTED);
                r.setRejectedRequests(findAllByListIds(toReject));
            }

            return r;
        });

        if (result != null && result.getConfirmedRequests() != null && !result.getConfirmedRequests().isEmpty()) {
            Boolean ok = eventClient.incrementConfirmedRequests(eventId, result.getConfirmedRequests().size());
            if (ok == null || !ok) {
                throw new RequestModerationException("Не удалось обновить confirmedRequests");
            }
        }

        return result;
    }

    @Override
    public Optional<EventRequestDto> getByEventIdAndRequesterId(Long eventId, Long userId) {
        return eventRequestRepository.findByEventIdAndRequesterId(eventId, userId)
                .map(EventRequestMapper::mapToEventRequestDto);
    }

    private List<EventRequestDto> findAllByListIds(List<Long> ids) {
        log.info("Получаем все обновленные заявки по id={}", ids);

        List<EventRequest> requests = eventRequestRepository.findByIdIn(ids);
        log.info("Возвращаем список обновленных заявок {}", requests);

        return requests.stream().map(EventRequestMapper::mapToEventRequestDto).toList();
    }

    private void updateStatusAllRequest(List<Long> ids, Status status) {
        log.info("Обновляем статус для заявок id={} на {}", ids, status);

        int update = eventRequestRepository.updateStatusForRequestsIds(ids, status);
        log.info("Количество обновленных записей: {}", update);
    }
}