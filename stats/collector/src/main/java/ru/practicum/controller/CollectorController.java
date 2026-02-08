package ru.practicum.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.mapper.UserActionMapper;
import ru.practicum.service.CollectorService;
import ru.yandex.practicum.grpc.stats.action.UserActionControllerGrpc;
import stats.messages.collector.UserAction;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class CollectorController extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final CollectorService collectorService;
    private final UserActionMapper userActionMapper;

    @Value("${kafka.topics.user-action}")
    private String topic;

    @Override
    public void collectUserAction(UserAction.UserActionProto request, StreamObserver<Empty> responseObserver) {
        log.info("Получение информации о действии пользователя {}", request);
        try {
            UserActionAvro avro = userActionMapper.mapToAvro(request);
            collectorService.send(topic, avro);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getLocalizedMessage())
                    .withCause(e)
                    .asRuntimeException()
            );
        }
    }
}