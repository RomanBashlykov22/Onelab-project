package kz.romanb.onelabproject.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class OperationDto {
    private Long id;
    private BigDecimal amount;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate date;
    @JsonProperty("bankAccount")
    private BankAccountDto bankAccountDto;
    @JsonProperty("costCategory")
    private CostCategoryDto costCategoryDto;
}
