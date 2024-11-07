package kz.romanb.onelabproject.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Информация о счете")
public class BankAccountDto {
    @Schema(description = "ID счета", example = "1")
    private Long id;
    @Schema(description = "Имя счета", example = "Kaspi")
    private String name;
    @Schema(description = "Баланс счета", example = "1000")
    private BigDecimal balance;
    @Schema(description = "Данные о пользователе-хозяине счета")
    @JsonProperty("user")
    private UserDto userDto;
}
