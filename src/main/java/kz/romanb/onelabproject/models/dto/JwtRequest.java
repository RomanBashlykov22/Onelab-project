package kz.romanb.onelabproject.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JwtRequest {

    @Email(message = "Введите e-mail")
    private String email;

    @NotBlank(message = "Пароль не должен быть пустым")
    private String password;
}