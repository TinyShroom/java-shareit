package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.logging.Logging;
import ru.practicum.shareit.request.dto.RequestCreateDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;
import ru.practicum.shareit.request.service.RequestService;

import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @Logging
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto create(@RequestHeader(HEADER_USER_ID) long userId,
                             @Validated
                             @RequestBody
                             RequestCreateDto requestCreateDto) {
        return requestService.create(userId, requestCreateDto);
    }

    @Logging
    @GetMapping("/{requestId}")
    public RequestWithItemsDto get(@RequestHeader(HEADER_USER_ID) long userId,
                                   @PathVariable long requestId) {
        return requestService.findById(userId, requestId);
    }

    @Logging
    @GetMapping
    public List<RequestWithItemsDto> get(@RequestHeader(HEADER_USER_ID) long userId) {
        return requestService.findByUserId(userId);
    }

    @Logging
    @GetMapping("/all")
    public List<RequestWithItemsDto> getAll(@RequestHeader(HEADER_USER_ID) long userId,
                                            @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                            @RequestParam(required = false) @Min(1) Integer size) {
        return requestService.findAll(userId, from, size);
    }
}
