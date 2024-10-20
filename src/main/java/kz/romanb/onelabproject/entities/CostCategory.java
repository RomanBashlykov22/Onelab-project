package kz.romanb.onelabproject.entities;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostCategory {
    private Long id;
    private Long userId;
    private String name;
    private CostCategoryType categoryType;

    public enum CostCategoryType {
        EXPENSE, INCOME
    }
}
