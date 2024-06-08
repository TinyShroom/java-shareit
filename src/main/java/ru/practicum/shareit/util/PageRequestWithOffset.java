package ru.practicum.shareit.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class PageRequestWithOffset extends PageRequest {
    /**
     * Creates a new {@link PageRequest} with sort parameters applied.
     *
     * @param from zero-based from index, must not be negative.
     * @param size the size of the page to be returned, must be greater than 0.
     * @param sort must not be {@literal null}, use {@link Sort#unsorted()} instead.
     */
    protected PageRequestWithOffset(int from, int size, Sort sort) {
        super(from / size, size, sort);
    }

    public static PageRequestWithOffset of(int from, int size, Sort sort) {
        return new PageRequestWithOffset(from, size, sort);
    }

    public static PageRequestWithOffset of(int from, int size) {
        return new PageRequestWithOffset(from, size, Sort.unsorted());
    }
}
