package kz.romanb.onelabproject.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "operation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Operation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    @Temporal(TemporalType.DATE)
    @Column(name = "date", nullable = false)
    @Builder.Default
    private LocalDate date = LocalDate.now();
    @ManyToOne
    @JoinColumn(name = "bank_account_id")
    private BankAccount bankAccount;
    @ManyToOne
    @JoinColumn(name = "cost_category_id")
    private CostCategory costCategory;

    @Override
    public String toString() {
        if (costCategory.getCategoryType().equals(CostCategory.CostCategoryType.EXPENSE))
            return "Операция " + id + ". Дата - " + date + ". Потрачено " + amount.toString() + " на категорию " + costCategory.getName() + " со счета " + bankAccount.getName();
        else
            return "Операция " + id + ". Дата - " + date + ". Получено " + amount.toString() + " " + costCategory.getName() + " на счет " + bankAccount.getName();
    }
}
