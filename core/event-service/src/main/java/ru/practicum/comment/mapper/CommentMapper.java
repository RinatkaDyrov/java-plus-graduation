package ru.practicum.comment.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.event.State;
import ru.practicum.comment.model.Comment;
import ru.practicum.dto.user.UserDto;
import ru.practicum.event.model.Event;


import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {

    public static Comment toEntity(NewCommentDto dto, UserDto user, Event event) {
        return Comment.builder()
                .text(dto.getText())
                .created(LocalDateTime.now())
                .creatorId(user.getId())
                .event(event)
                .title(event.getTitle())
                .state(State.PENDING)
                .build();
    }

    public static CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .creatorName(comment.getName())
                .eventName(comment.getTitle())
                .state(comment.getState())
                .build();
    }
}
