package ru.practicum.mapper;

import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.ewm.stats.avro.ActionType;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import stats.messages.collector.UserAction;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface UserActionMapper {

    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "eventId", target = "eventId")
    @Mapping(source = "actionType", target = "actionType", qualifiedByName = "mapToAvroActionType")
    @Mapping(source = "timestamp", target = "timestamp", qualifiedByName = "mapTimestampToInstant")
    UserActionAvro mapToAvro(UserAction.UserActionProto proto);

    @Named("mapToAvroActionType")
    default ActionType mapToAvroActionType(UserAction.ActionTypeProto actionType) {
        if (actionType == null) return null;
        return switch (actionType) {
            case ACTION_VIEW -> ActionType.VIEW;
            case ACTION_REGISTER -> ActionType.REGISTER;
            case ACTION_LIKE -> ActionType.LIKE;
            case UNRECOGNIZED -> null;
        };
    }

    @Named("mapTimestampToInstant")
    default Instant mapTimestampToInstant(Timestamp ts) {
        if (ts == null) return null;
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }
}
