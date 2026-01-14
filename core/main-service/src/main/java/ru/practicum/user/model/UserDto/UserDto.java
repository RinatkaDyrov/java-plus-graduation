package ru.practicum.user.model.UserDto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private Long id;
    @NotBlank(message = "Пользователь должен иметь email")
    @Email(message = "email не соответствует формату")
    @Size(min = 6, message = "email не может быть меньше 6 символов")
    @Size(max = 254, message = "mail не может быть больше 255 символов ")
    private String email;
    @NotBlank(message = "Пользователь должен иметь не пустое имя")
    @Size(min = 2, message = "Имя не может быть меньше 2 символов")
    @Size(max = 250, message = "Имя не может быть больше 250 символов ")
    private String name;
}
