package kz.romanb.onelabproject.entities;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "cost_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "name", nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false)
    private CostCategoryType categoryType;

    public enum CostCategoryType {
        EXPENSE, INCOME
    }
}
