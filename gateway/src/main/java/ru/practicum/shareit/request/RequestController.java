package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.logging.Logging;
import ru.practicum.shareit.request.dto.RequestCreateDto;

import javax.validation.constraints.Min;
import java.util.Map;

@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestClient requestClient;
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @Logging
    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(HEADER_USER_ID) long userId,
                                         @Validated
                                         @RequestBody
                                         RequestCreateDto requestCreateDto) {
        return requestClient.create(userId, requestCreateDto);
    }

    @Logging
    @GetMapping("/{requestId}")
    public ResponseEntity<Object> get(@RequestHeader(HEADER_USER_ID) long userId,
                                      @PathVariable long requestId) {
        return requestClient.getById(userId, requestId);
    }

    @Logging
    @GetMapping
    public ResponseEntity<Object> get(@RequestHeader(HEADER_USER_ID) long userId) {
        return requestClient.getByUserId(userId);
    }

    @Logging
    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader(HEADER_USER_ID) long userId,
                                         @RequestParam(defaultValue = "0") @Min(0) int from,
                                         @RequestParam(defaultValue = "10") @Min(1) int size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return requestClient.getAll(userId, parameters);
    }
}