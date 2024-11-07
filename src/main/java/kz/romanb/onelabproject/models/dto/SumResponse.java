package kz.romanb.onelabproject.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Расчет доходов и расходов")
public class SumResponse {
    @Schema(description = "Количесво переданных операций")
    private Integer amountOfOperations;
    @Schema(description = "Итог по расходам")
    private BigDecimal expense;
    @Schema(description = "Итог по доходам")
    private BigDecimal income;

    @Override
    public String toString() {
        return "Итого:\n" +
                "Количество операций - " + amountOfOperations +
                "\nРасходов на " + expense +
                "\nДоходов на " + income;
    }
}
