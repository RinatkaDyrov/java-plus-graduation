package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
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
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(readOnly = true)
    @Override
    public List<EventRequestDto> getUsersRequests(Long userId) {
        userClient.getUserById(userId).orElseThrow(() -> new NotFoundException("EventRequest", userId));
        return eventRequestRepository.findAllByRequesterId(userId).stream()
                .map(EventRequestMapper::mapToEventRequestDto).toList();
    }

    @Override
    public EventRequestDto createRequest(Long userId, Long eventId) {
        log.info("Начинаем создание заявки на участие в мероприятии id = {} от пользователя id = {}", eventId, userId);

        UserDto user = userClient.getUserById(userId).orElseThrow(() -> new NotFoundException("EventRequest", userId));
        EventFullDto event = eventClient.getEventById(eventId).orElseThrow(() ->
                new NotFoundException("EventRequest", eventId));

        if (eventRequestRepository.findByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new RequestModerationException(eventId, "Заявка уже была отправлена");
        }

        if (event.getParticipantLimit() > 0 && event.getParticipantLimit() >= (event.getConfirmedRequests())) {
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
            applicationEventPublisher.publishEvent(new ConfirmedRequestsChangedEvent(saved.getEventId(), 1));
        }

        return EventRequestMapper.mapToEventRequestDto(saved);
    }

    @Override
    public EventRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отменяем заявку id={} пользователем id={}", requestId, userId);

        EventRequestDto result = transactionTemplate.execute(tx -> {
            EventRequest eventRequest = eventRequestRepository.findById(requestId).orElseThrow(() ->
                    new NotFoundException("EventRequest", requestId));

            if (!eventRequest.getRequesterId().equals(userId)){
                throw new NotValidUserException(userId);
            }

            Status previousStatus = eventRequest.getStatus();

            int updated = eventRequestRepository.updateStatus(requestId, Status.CANCELED);
            if (updated != 1) {
                throw new ConflictException("Не удалось отменить заявку");
            }

            eventRequest.setStatus(Status.CANCELED);

            if (previousStatus == Status.CONFIRMED) {
                applicationEventPublisher.publishEvent(
                        new ConfirmedRequestsChangedEvent(eventRequest.getEventId(), -1)
                );
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
        eventClient.getEventById(eventId).orElseThrow(() ->
                new NotFoundException("EventRequest", eventId));

        return eventRequestRepository.findAllByEventId(eventId).stream()
                .map(EventRequestMapper::mapToEventRequestDto)
                .toList();
    }

    @Override
    public EventRequestUpdateResult updateRequestState(Long userId, Long eventId,
                                                       EventRequestUpdateDto updateDto) {
        log.info("Начинаем обновление заявок для событий id={} пользователем id={}", eventId, userId);

        String status = updateDto.getStatus();
        List<Long> requestIds = updateDto.getRequestIds();

        UserDto user = userClient.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));
        log.info("Определен инициатор события {}", user);
        EventFullDto event = eventClient.getEventById(eventId)
                .orElseThrow(() -> new NotFoundException("Event", eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            log.error("Пользователь id={} не является инициатором события id={}", userId, eventId);
            throw new ConflictException("У пользователя нет доступа к данному событию");
        }

        List<EventRequest> requests = eventRequestRepository.findByRequestIds(requestIds);
        if (requests.stream().anyMatch(eventRequest -> !eventRequest.getStatus().equals(Status.PENDING))) {
            log.error("В списке есть заявка не находящаяся в статусе ожидания");
            throw new RequestModerationException(eventId, "Можно принимать заявки только в статусе ожидания");
        }

        EventRequestUpdateResult result = transactionTemplate.execute(tx -> {
            EventFullDto fresh = eventClient.getEventById(eventId)
                    .orElseThrow(() -> new NotFoundException("Event", eventId));

            int limitFresh = fresh.getParticipantLimit();
            int confirmedFresh = fresh.getConfirmedRequests();

            if (limitFresh > 0 && confirmedFresh >= limitFresh && !status.equalsIgnoreCase("rejected")) {
                throw new RequestModerationException(eventId, "Лимит заявок исчерпан");
            }

            int available = (limitFresh == 0) ? Integer.MAX_VALUE : (limitFresh - confirmedFresh);
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
            applicationEventPublisher.publishEvent(
                    new ConfirmedRequestsChangedEvent(eventId, result.getConfirmedRequests().size())
            );
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
        return requests.stream()
                .map(EventRequestMapper::mapToEventRequestDto)
                .toList();
    }

    private void updateStatusAllRequest(List<Long> ids, Status status) {
        log.info("Обновляем статус для заявок id={} на {}", ids, status);
        int update = eventRequestRepository.updateStatusForRequestsIds(ids, status);
        log.info("Количество обновленных записей: {}", update);
    }
}
