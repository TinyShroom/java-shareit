package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.CommentShort;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    Comment dtoToComment(CommentDtoRequest commentDto, User author, Item item, LocalDateTime created);

    @Mapping(target = "authorName", source = "comment.author.name")
    CommentDtoResponse commentToDto(Comment comment);

    CommentDtoResponse shortToDtoResponse(CommentShort comment);
}
