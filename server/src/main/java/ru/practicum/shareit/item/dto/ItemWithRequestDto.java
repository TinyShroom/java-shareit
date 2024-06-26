package ru.practicum.shareit.item.dto;

import lombok.Data;

@Data
public class ItemWithRequestDto {
    private long id;
    private Long requestId;
    private String name;
    private String description;
    private boolean available;
}
