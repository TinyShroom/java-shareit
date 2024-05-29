package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.RequestCreateDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;

import java.util.List;

public interface RequestService {

    RequestDto create(long userId, RequestCreateDto requestCreateDto);

    RequestWithItemsDto findById(long userId, long requestId);

    List<RequestWithItemsDto> findByUserId(long userId);

    List<RequestWithItemsDto> findAll(long userId, Pageable pageable);
}
