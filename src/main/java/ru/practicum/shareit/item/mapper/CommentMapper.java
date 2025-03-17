package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "authorName", source = "author.name")
    CommentDto mapToCommentDto(Comment comment);

    Comment mapToComment(CommentDto commentDto);

    List<CommentDto> mapToCommentDto(List<Comment> comments);
}
