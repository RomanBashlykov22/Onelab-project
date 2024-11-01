package kz.romanb.onelabproject.models.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SumResponse {
    private Integer amountOfOperations;
    private BigDecimal expense;
    private BigDecimal income;
}
