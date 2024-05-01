package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.constraint.AddingConstraint;
import ru.practicum.shareit.constraint.NullOrNotBlank;
import ru.practicum.shareit.constraint.PatchConstraint;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private long id;
    private String name;
    @Email(groups = {AddingConstraint.class, PatchConstraint.class})
    @NotBlank(groups = {AddingConstraint.class})
    @NullOrNotBlank(groups = {PatchConstraint.class})
    private String email;
}
