package kz.romanb.onelabproject.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {

    @Email(message = "Введите e-mail")
    private String email;

    @NotBlank(message = "Пароль не должен быть пустым")
    private String password;

    @NotBlank(message = "Юзернейм не должен быть пустым")
    private String username;
}