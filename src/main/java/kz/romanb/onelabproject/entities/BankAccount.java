package kz.romanb.onelabproject.entities;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount {
    private Long id;
    private Long userId;
    private String name;
    private BigDecimal balance;
}
