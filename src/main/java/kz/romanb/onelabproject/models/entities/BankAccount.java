package kz.romanb.onelabproject.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bank_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;
    @OneToMany(
            cascade = CascadeType.ALL,
            mappedBy = "bankAccount",
            orphanRemoval = true
    )
    @Builder.Default
    private List<Operation> operations = new ArrayList<>();
}
