package ru.practicum.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.mapper.UserActionMapper;
import ru.practicum.service.CollectorService;
import ru.yandex.practicum.grpc.stats.action.UserActionControllerGrpc;
import stats.message.userAction.UserAction;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class CollectorController extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final CollectorService collectorService;
    private final UserActionMapper userActionMapper;

    @Value("${kafka.topics.user-action}")
    private String topic;

    @Override
    public void collectionUserAction(UserAction.UserActionProto request, StreamObserver<Empty> responseObserver) {
        log.info("Получение информации о действии пользователя {}", request);
        try {
            collectorService.send(topic, userActionMapper.mapToAvro(request));
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}