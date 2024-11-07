package kz.romanb.onelabproject.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Данные пользователя для входа")
public record JwtRequest(
        @Schema(description = "Адрес электронной почты пользователя", example = "user@gmail.com")
        @Email(message = "Введите e-mail")
        String email,
        @Schema(description = "Пароль пользователя")
        @NotBlank(message = "Пароль не должен быть пустым")
        String password
) {
}