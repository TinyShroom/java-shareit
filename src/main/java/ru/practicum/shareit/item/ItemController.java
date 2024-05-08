package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constraint.Update;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.logging.Logging;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @Logging
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestHeader(HEADER_USER_ID) long userId,
                          @RequestBody @Valid ItemDto item) {
        return itemService.create(userId, item);
    }

    @Logging
    @GetMapping("/{id}")
    public ItemDtoResponse getById(@RequestHeader(HEADER_USER_ID) long userId, @PathVariable long id) {
        return itemService.findById(userId, id);
    }

    @Logging
    @GetMapping
    public List<ItemDtoResponse> getAll(@RequestHeader(HEADER_USER_ID) long userId) {
        return itemService.getAll(userId);
    }

    @Logging
    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader(HEADER_USER_ID) long userId,
                          @PathVariable long id,
                          @RequestBody @Validated(Update.class) ItemDto item) {
        item.setId(id);
        return itemService.update(userId, item);
    }

    @Logging
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader(HEADER_USER_ID) long userId,
                       @PathVariable Long id) {
        itemService.delete(userId, id);
    }

    @Logging
    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.search(text);
    }

    @Logging
    @PostMapping("/{itemId}/comment")
    public CommentDtoResponse createComment(@RequestHeader(HEADER_USER_ID) long userId,
                                            @PathVariable long itemId,
                                            @RequestBody @Valid CommentDtoRequest commentDtoRequest) {
        return itemService.createComment(userId, itemId, commentDtoRequest);
    }
}
