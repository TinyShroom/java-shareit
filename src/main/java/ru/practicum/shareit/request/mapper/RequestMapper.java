package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.ItemWithRequestDto;
import ru.practicum.shareit.request.Request;
import ru.practicum.shareit.request.dto.RequestCreateDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "id", ignore = true)
    Request dtoToRequest(RequestCreateDto requestCreateDto, User user, LocalDateTime created);

    RequestDto requestToDto(Request request);

    RequestWithItemsDto requestToDto(Request request, List<ItemWithRequestDto> items);
}
