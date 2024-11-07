package kz.romanb.onelabproject.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Ответ сервера с токенами пользователя")
public class JwtResponse {
    @Schema(description = "Тип токена")
    private final String type = "Bearer";
    @Schema(description = "Access-токен")
    private String accessToken;
    @Schema(description = "Refresh-токен")
    private String refreshToken;
}
