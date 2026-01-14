package ru.practicum.comment.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {

    public static Comment toEntity(NewCommentDto dto, User user, Event event) {
        return Comment.builder()
                .text(dto.getText())
                .created(LocalDateTime.now())
                .creator(user)
                .event(event)
                .state(State.PENDING)
                .build();
    }

    public static CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .creatorName(comment.getCreator().getName())
                .eventName(comment.getEvent().getTitle())
                .state(comment.getState())
                .build();
    }
}
