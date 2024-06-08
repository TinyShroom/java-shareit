package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RequestDto {
    private Long id;
    private String description;
    private LocalDateTime created;
}
