package kz.romanb.onelabproject.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usrs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    @OneToMany(
            cascade = CascadeType.ALL,
            mappedBy = "user",
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    @Builder.Default
    private List<BankAccount> bankAccounts = new ArrayList<>();
    @OneToMany(
            cascade = CascadeType.ALL,
            mappedBy = "user",
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    @Builder.Default
    private List<CostCategory> costCategories = new ArrayList<>();
}
