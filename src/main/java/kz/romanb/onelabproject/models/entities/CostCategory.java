package kz.romanb.onelabproject.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


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
    @OneToMany(
            cascade = CascadeType.ALL,
            mappedBy = "costCategory",
            orphanRemoval = true
    )
    @Builder.Default
    private List<Operation> costCategoryOperations = new ArrayList<>();

    public enum CostCategoryType {
        EXPENSE, INCOME
    }
}
