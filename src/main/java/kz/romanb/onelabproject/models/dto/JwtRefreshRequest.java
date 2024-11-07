package kz.romanb.onelabproject.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Обновление JWT-токенов")
public record JwtRefreshRequest(@Schema(description = "Текущий Refresh-токен") String refreshToken) {
}
