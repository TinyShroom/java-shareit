package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constraint.Update;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestHeader(HEADER_USER_ID) long userId,
                          @RequestBody @Valid ItemDto item) {
        return itemService.create(userId, item);
    }

    @GetMapping("/{id}")
    public ItemDto getById(@PathVariable long id) {
        return itemService.findById(id);
    }

    @GetMapping
    public List<ItemDto> getAll(@RequestHeader(HEADER_USER_ID) long userId) {
        return itemService.getAll(userId);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader(HEADER_USER_ID) long userId,
                       @PathVariable long id,
                       @RequestBody @Validated(Update.class) ItemDto item) {
        return itemService.update(userId, id, item);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader(HEADER_USER_ID) long userId,
                       @PathVariable Long id) {
        itemService.delete(userId, id);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.findBy(text);
    }
}
