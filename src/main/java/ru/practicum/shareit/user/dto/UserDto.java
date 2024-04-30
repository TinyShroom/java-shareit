package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.constraint.AddingConstraint;
import ru.practicum.shareit.constraint.PatchConstraint;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
public class UserDto {
    private long id;
    private String name;
    @Email(groups = {AddingConstraint.class, PatchConstraint.class})
    @NotBlank(groups = {AddingConstraint.class})
    private String email;
}
