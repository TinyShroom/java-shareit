package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.logging.Logging;
import ru.practicum.shareit.util.PageRequestWithOffset;

import java.util.List;

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
                          @RequestBody ItemDto item) {
        return itemService.create(userId, item);
    }

    @Logging
    @GetMapping("/{id}")
    public ItemWithBookingsDto getById(@RequestHeader(HEADER_USER_ID) long userId, @PathVariable long id) {
        return itemService.findById(userId, id);
    }

    @Logging
    @GetMapping
    public List<ItemWithBookingsDto> getAll(@RequestHeader(HEADER_USER_ID) long userId,
                                            @RequestParam int from,
                                            @RequestParam int size) {
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("id"));
        return itemService.getAll(userId, pageable);
    }

    @Logging
    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader(HEADER_USER_ID) long userId,
                          @PathVariable long id,
                          @RequestBody ItemDto item) {
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
    public List<ItemDto> search(@RequestParam String text,
                                @RequestParam int from,
                                @RequestParam int size) {
        Pageable pageable = PageRequestWithOffset.of(from, size);
        return itemService.search(text, pageable);
    }

    @Logging
    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader(HEADER_USER_ID) long userId,
                                    @PathVariable long itemId,
                                    @RequestBody CommentCreateDto commentCreateDto) {
        return itemService.createComment(userId, itemId, commentCreateDto);
    }
}
