package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constraint.PatchConstraint;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto add(@RequestHeader("X-Sharer-User-Id") long userId,
                    @RequestBody @Valid ItemDto item) {
        log.info("POST /items: userId={}, item: {}", userId, item.toString());
        var result = itemService.add(userId, item);
        log.info("completion POST /items: userId={}, item: {}", userId, result);
        return result;
    }

    @GetMapping("/{id}")
    public ItemDto getById(@PathVariable long id) {
        log.info("GET /item: {}", id);
        var item = itemService.findById(id);
        log.info("completion GET /item: {}", item);
        return item;
    }

    @GetMapping
    public List<ItemDto> getAll(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET /items: all for userId {}", userId);
        var result = itemService.getAll(userId);
        log.info("completion GET /items: size {} for userId {}", result.size(), userId);
        return result;
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") long userId,
                       @PathVariable long id,
                       @RequestBody @Validated(PatchConstraint.class) ItemDto item) {
        log.info("PATCH /items: id={}, user={}, item={}", id, userId, item.toString());
        var result = itemService.update(userId, id, item);
        log.info("completion PATCH /items: id={}, user={}, item={}", id, userId, result);
        return result;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader("X-Sharer-User-Id") long userId,
                       @PathVariable Long id) {
        log.info("DELETE /items: itemId={}, userId={}", id, userId);
        itemService.delete(userId, id);
        log.info("completion DELETE /items: itemId={}, userId={} success", id, userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.info("GET /search: text={}", text);
        var items = itemService.findByText(text);
        log.info("completion GET /search: text={}, found size={}", text, items.size());
        return items;
    }
}
