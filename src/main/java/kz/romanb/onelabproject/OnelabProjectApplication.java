package kz.romanb.onelabproject;

import kz.romanb.onelabproject.models.entities.*;
import kz.romanb.onelabproject.repositories.BankAccountRepository;
import kz.romanb.onelabproject.repositories.CostCategoryRepository;
import kz.romanb.onelabproject.repositories.OperationRepository;
import kz.romanb.onelabproject.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

@SpringBootApplication(scanBasePackages = "kz.romanb.onelabproject")
public class OnelabProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnelabProjectApplication.class, args);
    }

    @Bean
    public CommandLineRunner dataLoader(
            UserRepository userRepository,
            BankAccountRepository bankAccountRepository,
            CostCategoryRepository costCategoryRepository,
            OperationRepository operationRepository,
            PasswordEncoder passwordEncoder){
        return args -> {
            User user = User.builder()
                    .email("roman.bash14@mail.ru")
                    .username("ramioris")
                    .password(passwordEncoder.encode("123"))
                    .roles(Set.of(Role.ADMIN, Role.USER))
                    .isCredentialsNonExpired(true)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .isAccountNonExpired(true)
                    .build();
            userRepository.save(user);

            BankAccount bankAccount1 = BankAccount.builder()
                    .user(user)
                    .name("Kaspi")
                    .balance(new BigDecimal("12266.12"))
                    .build();
            BankAccount bankAccount2 = BankAccount.builder()
                    .user(user)
                    .name("Jusan")
                    .balance(new BigDecimal("3668.13"))
                    .build();
            bankAccountRepository.save(bankAccount1);
            bankAccountRepository.save(bankAccount2);

            CostCategory costCategory1 = CostCategory.builder()
                    .name("Sport")
                    .categoryType(CostCategory.CostCategoryType.EXPENSE)
                    .user(user)
                    .build();
            CostCategory costCategory2 = CostCategory.builder()
                    .user(user)
                    .categoryType(CostCategory.CostCategoryType.INCOME)
                    .name("Salary")
                    .build();
            costCategoryRepository.save(costCategory1);
            costCategoryRepository.save(costCategory2);

            Operation operation1 = Operation.builder()
                    .bankAccount(bankAccount1)
                    .costCategory(costCategory1)
                    .amount(new BigDecimal(6000))
                    .date(LocalDate.of(2024, 10, 13))
                    .build();
            Operation operation2 = Operation.builder()
                    .bankAccount(bankAccount1)
                    .costCategory(costCategory2)
                    .amount(new BigDecimal("9831.07"))
                    .date(LocalDate.of(2024, 10, 1))
                    .build();
            operationRepository.save(operation1);
            operationRepository.save(operation2);
        };
    }
}
