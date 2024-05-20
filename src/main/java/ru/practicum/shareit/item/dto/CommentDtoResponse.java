package ru.practicum.shareit.item.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDtoResponse {
    private Long id;
    private String authorName;
    private String text;
    private LocalDateTime created;
}
