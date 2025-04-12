package ru.practicum.shareit.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.ValidationGroup;

@Data
@Builder
public class UserRequestDto {

    @NotNull(groups = ValidationGroup.OnCreate.class)
    @Pattern(regexp = "\\S+")
    @Email
    private String email;

    private String name;
}
