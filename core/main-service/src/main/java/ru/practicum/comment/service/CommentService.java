package ru.practicum.comment.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentSearchParam;
import ru.practicum.comment.dto.CommentUpdateRequestDto;
import ru.practicum.comment.dto.NewCommentDto;

import java.util.List;

public interface CommentService {

    List<CommentDto> getCommentsByUser(Long userId);

    List<CommentDto> getCommentsByEvent(Long eventId);

    CommentDto createComment(Long userId, Long eventId, NewCommentDto dto);

    CommentDto updateComment(Long userId, Long commentId, NewCommentDto dto);

    void deleteComment(Long userId, Long commentId);

    void deleteCommentByAdmin(Long commentId);

    CommentDto getById(Long commentId);

    List<CommentDto> updateCommentState(CommentUpdateRequestDto request);

    List<CommentDto> getCommentWithParamAdmin(CommentSearchParam commentSearchParam, Pageable page);
}

