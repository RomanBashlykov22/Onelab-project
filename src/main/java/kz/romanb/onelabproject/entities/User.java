package kz.romanb.onelabproject.entities;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    private String name;
    @Builder.Default
    private List<BankAccount> bankAccounts = new ArrayList<>();
    @Builder.Default
    private List<CostCategory> costCategories = new ArrayList<>();
}
