package kz.romanb.onelabproject.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record JwtRequest(
        @Email(message = "Введите e-mail")
        String email,
        @NotBlank(message = "Пароль не должен быть пустым")
        String password
) {
}