package kz.romanb.onelabproject.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Информация об операции")
public class OperationDto {
    @Schema(description = "ID операции", example = "1")
    private Long id;
    @Schema(description = "Сумма", example = "1000")
    private BigDecimal amount;
    @Schema(description = "Дата создания операции", example = "10.10.2024")
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate date;
    @Schema(description = "Данные о счете, с которым проведена операция")
    @JsonProperty("bankAccount")
    private BankAccountDto bankAccountDto;
    @Schema(description = "Данные о категории, с которой проведена операция")
    @JsonProperty("costCategory")
    private CostCategoryDto costCategoryDto;
}
