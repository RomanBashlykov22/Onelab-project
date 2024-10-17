package kz.romanb.onelabproject.entities;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Operation {
    private long id;
    private BigDecimal amount;
    @Builder.Default
    private LocalDate date = LocalDate.now();
    private BankAccount bankAccount;
    private CostCategory costCategory;

    @Override
    public String toString() {
        if (costCategory.getCategoryType().equals(CostCategory.CostCategoryType.EXPENSE))
            return "Операция " + id + ". Дата - " + date + ". Потрачено " + amount.toString() + " на категорию " + costCategory.getName() + " со счета " + bankAccount.getName();
        else
            return "Операция " + id + ". Дата - " + date + ". Получено " + amount.toString() + " " + costCategory.getName() + " на счет " + bankAccount.getName();
    }
}
