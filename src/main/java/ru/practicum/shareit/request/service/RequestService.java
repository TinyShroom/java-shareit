package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.RequestCreateDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;

import java.util.List;

public interface RequestService {

    RequestDto create(long userId, RequestCreateDto requestCreateDto);

    RequestWithItemsDto findById(long userId, long requestId);

    List<RequestWithItemsDto> findByUserId(long userId);

    List<RequestWithItemsDto> findAll(long userId, int from, Integer size);
}
