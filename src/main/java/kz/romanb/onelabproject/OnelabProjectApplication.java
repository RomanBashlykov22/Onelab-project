package kz.romanb.onelabproject;

import kz.romanb.onelabproject.entities.BankAccount;
import kz.romanb.onelabproject.entities.CostCategory;
import kz.romanb.onelabproject.entities.Operation;
import kz.romanb.onelabproject.entities.User;
import kz.romanb.onelabproject.services.CostTrackerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class OnelabProjectApplication {

    public static void main(String[] args) {
        var context = SpringApplication.run(OnelabProjectApplication.class, args);
        CostTrackerService costTrackerService = context.getBean(CostTrackerService.class);

        User roman = costTrackerService.findUserById(1L).get();

        getUserInfo(roman);

        System.out.println("---------------------------------------");

        User alex = costTrackerService.findUserById(2L).get();

        getUserInfo(alex);

        costTrackerService.createOperation(roman.getBankAccounts().get(0), roman.getCostCategories().get(1), new BigDecimal(515));
        costTrackerService.createOperation(roman.getBankAccounts().get(1), roman.getCostCategories().get(2), new BigDecimal(1000));
        costTrackerService.createOperation(roman.getBankAccounts().get(0), roman.getCostCategories().get(1), new BigDecimal(8866));

        System.out.println("Все операции: ");
        List<Operation> allOperations = costTrackerService.findAllOperations();
        allOperations.forEach(System.out::println);
        System.out.println();

        System.out.println("Все операции пользователя " + roman.getName());
        List<Operation> romanOperations = costTrackerService.findAllOperationsByUser(roman);
        romanOperations.forEach(System.out::println);
        System.out.println("Расходы - " + costTrackerService.getSum(romanOperations, CostCategory.CostCategoryType.EXPENSE));
        System.out.println("Доходы - " + costTrackerService.getSum(romanOperations, CostCategory.CostCategoryType.INCOME));
        System.out.println();

        System.out.println("Все операции пользователя " + roman.getName() + " по категории " + roman.getCostCategories().get(1).getName());
        List<Operation> romanCostCategoryOperations = costTrackerService.findAllOperationsByCostCategory(roman.getCostCategories().get(1));
        romanCostCategoryOperations.forEach(System.out::println);
        System.out.println("Сумма - " + costTrackerService.getSum(romanCostCategoryOperations, roman.getCostCategories().get(1).getCategoryType()));
        System.out.println();

        System.out.println("Все операции пользователя " + roman.getName() + " на 17.10.2024: ");
        List<Operation> romanDateOperations = costTrackerService.findAllOperationsForDate(LocalDate.of(2024, 10, 17));
        romanDateOperations.forEach(System.out::println);
        System.out.println("Расходы - " + costTrackerService.getSum(romanDateOperations, CostCategory.CostCategoryType.EXPENSE));
        System.out.println("Доходы - " + costTrackerService.getSum(romanDateOperations, CostCategory.CostCategoryType.INCOME));
        System.out.println();

        System.out.println("Все операции пользователя " + roman.getName() + " c 03.10.2024 по 13.10.2024: ");
        List<Operation> romanDatesOperations = costTrackerService.findAllOperationsBetweenDates(LocalDate.of(2024, 10, 3), LocalDate.of(2024, 10, 13));
        romanDatesOperations.forEach(System.out::println);
        System.out.println("Расходы - " + costTrackerService.getSum(romanDatesOperations, CostCategory.CostCategoryType.EXPENSE));
        System.out.println("Доходы - " + costTrackerService.getSum(romanDatesOperations, CostCategory.CostCategoryType.INCOME));
        System.out.println();
    }

    private static void getUserInfo(User user) {
        System.out.println("Пользователь " + user.getId() + " - " + user.getName());
        System.out.println();

        System.out.println("Счета: ");
        user.getBankAccounts().stream()
                .map(b -> b.getId() + " - " + b.getName() + ". На балансе - " + b.getBalance().toString())
                .forEach(System.out::println);
        System.out.println();

        System.out.println("Категории расходов: ");
        user.getCostCategories().stream()
                .filter(c -> c.getCategoryType().equals(CostCategory.CostCategoryType.EXPENSE))
                .map(c -> c.getId() + " - " + c.getName())
                .forEach(System.out::println);
        System.out.println();

        System.out.println("Категории доходов: ");
        user.getCostCategories().stream()
                .filter(c -> c.getCategoryType().equals(CostCategory.CostCategoryType.INCOME))
                .map(c -> c.getId() + " - " + c.getName())
                .forEach(System.out::println);
        System.out.println();
    }

    @Bean
    public CommandLineRunner dataLoader(CostTrackerService costTrackerService) {
        return args -> {
            User roman = costTrackerService.createNewUser(User.builder()
                    .id(1L)
                    .name("Roman")
                    .build()
            );
            costTrackerService.addNewBankAccountToUser(roman,
                    BankAccount.builder()
                            .id(1L)
                            .name("Kaspi")
                            .balance(new BigDecimal("20666.52"))
                            .build()
            );
            costTrackerService.addNewBankAccountToUser(roman,
                    BankAccount.builder()
                            .id(2L)
                            .name("Jusan")
                            .balance(new BigDecimal("5668.13"))
                            .build()
            );
            costTrackerService.addNewCostCategoryToUser(roman,
                    CostCategory.builder()
                            .id(1L)
                            .name("Sport")
                            .categoryType(CostCategory.CostCategoryType.EXPENSE)
                            .build()
            );
            costTrackerService.addNewCostCategoryToUser(roman,
                    CostCategory.builder()
                            .id(2L)
                            .name("Shopping")
                            .categoryType(CostCategory.CostCategoryType.EXPENSE)
                            .build()
            );
            costTrackerService.addNewCostCategoryToUser(roman,
                    CostCategory.builder()
                            .id(3L)
                            .name("Transport")
                            .categoryType(CostCategory.CostCategoryType.EXPENSE)
                            .build()
            );
            costTrackerService.addNewCostCategoryToUser(roman,
                    CostCategory.builder()
                            .id(4L)
                            .name("Work")
                            .categoryType(CostCategory.CostCategoryType.INCOME)
                            .build()
            );
            costTrackerService.createOperation(roman.getBankAccounts().get(0), roman.getCostCategories().get(1), new BigDecimal("1330.7"));
            costTrackerService.createOperation(roman.getBankAccounts().get(0), roman.getCostCategories().get(0), new BigDecimal(6000));
            costTrackerService.createOperation(roman.getBankAccounts().get(0), roman.getCostCategories().get(1), new BigDecimal("2861.52"));
            costTrackerService.createOperation(roman.getBankAccounts().get(0), roman.getCostCategories().get(3), new BigDecimal("9831.07"));
            costTrackerService.findOperationById(1L).get().setDate(LocalDate.of(2024, 10, 13));
            costTrackerService.findOperationById(2L).get().setDate(LocalDate.of(2024, 10, 13));
            costTrackerService.findOperationById(3L).get().setDate(LocalDate.of(2024, 10, 3));
            costTrackerService.findOperationById(4L).get().setDate(LocalDate.of(2024, 10, 1));

            User alex = costTrackerService.createNewUser(User.builder()
                    .id(2L)
                    .name("Alex")
                    .build()
            );
            costTrackerService.addNewBankAccountToUser(alex,
                    BankAccount.builder()
                            .id(3L)
                            .name("Kaspi")
                            .balance(new BigDecimal("17102.4"))
                            .build()
            );
            costTrackerService.addNewCostCategoryToUser(alex,
                    CostCategory.builder()
                            .id(5L)
                            .name("Shop")
                            .categoryType(CostCategory.CostCategoryType.EXPENSE)
                            .build()
            );
            costTrackerService.addNewCostCategoryToUser(alex,
                    CostCategory.builder()
                            .id(6L)
                            .name("Stipend")
                            .categoryType(CostCategory.CostCategoryType.INCOME)
                            .build()
            );
            costTrackerService.createOperation(alex.getBankAccounts().get(0), alex.getCostCategories().get(1), new BigDecimal(1330));
            costTrackerService.createOperation(alex.getBankAccounts().get(0), alex.getCostCategories().get(0), new BigDecimal(6000));
        };
    }
}
