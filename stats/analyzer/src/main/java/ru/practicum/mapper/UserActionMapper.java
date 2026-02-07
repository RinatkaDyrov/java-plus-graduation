package ru.practicum.mapper;

import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.model.UserAction;

public class UserActionMapper {

    public static UserAction mapToUserAction(UserActionAvro avro) {
        return UserAction.builder()
                .userId(avro.getUserId())
                .eventId(avro.getEventId())
                .rating(getMarkByActionType(avro))
                .timestamp(avro.getTimestamp())
                .build();
    }

    private static double getMarkByActionType(UserActionAvro avro) {
        return switch (avro.getActionType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}
