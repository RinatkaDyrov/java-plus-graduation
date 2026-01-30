package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.practicum.feignClient.event.EventClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmedRequestsChangedListener {

    private final EventClient eventClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ConfirmedRequestsChangedEvent e) {
        log.info("AFTER_COMMIT: количество подтвержденных заявок для eventId={} составило delta={}", e.eventId(), e.delta());
        boolean ok = eventClient.incrementConfirmedRequests(e.eventId(), e.delta());
        if (!ok) {
            log.error("Не получилось обновить confirmedRequests: eventId={}, delta={}", e.eventId(), e.delta());
        }
    }
}
