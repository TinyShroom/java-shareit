package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constraint.Update;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.logging.Logging;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemClient itemClient;

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @Logging
    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(HEADER_USER_ID) long userId,
                                         @RequestBody @Valid ItemDto item) {
        return itemClient.create(userId, item);
    }

    @Logging
    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@RequestHeader(HEADER_USER_ID) long userId, @PathVariable long id) {
        return itemClient.getById(userId, id);
    }

    @Logging
    @GetMapping
    public ResponseEntity<Object> getAll(@RequestHeader(HEADER_USER_ID) long userId,
                                         @RequestParam(defaultValue = "0") @Min(0) int from,
                                         @RequestParam(defaultValue = "10") @Min(1) int size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return itemClient.getAll(userId, parameters);
    }

    @Logging
    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@RequestHeader(HEADER_USER_ID) long userId,
                                         @PathVariable long id,
                                         @RequestBody @Validated(Update.class) ItemDto item) {
        return itemClient.update(userId, id, item);
    }

    @Logging
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@RequestHeader(HEADER_USER_ID) long userId,
                                         @PathVariable Long id) {
        return itemClient.delete(userId, id);
    }

    @Logging
    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(HEADER_USER_ID) long userId,
                                         @RequestParam String text,
                                         @RequestParam(defaultValue = "0") @Min(0) int from,
                                         @RequestParam(defaultValue = "10") @Min(1) int size) {
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return itemClient.search(parameters);
    }

    @Logging
    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader(HEADER_USER_ID) long userId,
                                                @PathVariable long itemId,
                                                @RequestBody @Valid CommentCreateDto commentCreateDto) {
        return itemClient.createComment(userId, itemId, commentCreateDto);
    }
}