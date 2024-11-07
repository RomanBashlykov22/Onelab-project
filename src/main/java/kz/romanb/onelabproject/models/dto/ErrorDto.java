package kz.romanb.onelabproject.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Описание ошибки")
public record ErrorDto(
        @Schema(description = "Ошибка")
        String error,
        @Schema(description = "Описание ошибки")
        String description) {
}