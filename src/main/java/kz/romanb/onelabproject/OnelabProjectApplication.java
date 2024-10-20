package kz.romanb.onelabproject;

import kz.romanb.onelabproject.entities.BankAccount;
import kz.romanb.onelabproject.entities.CostCategory;
import kz.romanb.onelabproject.entities.Operation;
import kz.romanb.onelabproject.entities.User;
import kz.romanb.onelabproject.services.BankAccountService;
import kz.romanb.onelabproject.services.CostCategoryService;
import kz.romanb.onelabproject.services.OperationService;
import kz.romanb.onelabproject.services.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class OnelabProjectApplication {

    public static void main(String[] args) {
        var context = SpringApplication.run(OnelabProjectApplication.class, args);
        OperationService operationService = context.getBean(OperationService.class);
        UserService userService = context.getBean(UserService.class);
        BankAccountService bankAccountService = context.getBean(BankAccountService.class);
        CostCategoryService costCategoryService = context.getBean(CostCategoryService.class);

        User user = userService.addNewUser(User.builder().name("Grisha").build());
//        User user = userService.findUserById(3L);
        bankAccountService.addNewBankAccountToUser(user,
                BankAccount.builder()
                        .name("Forte")
                        .balance(new BigDecimal(1107))
                        .userId(user.getId())
                        .build()
        );
//        bankAccountService.addNewBankAccountToUser(user,
//                BankAccount.builder()
//                        .name("Forte")
//                        .balance(new BigDecimal(1939))
//                        .userId(user.getId())
//                        .build()
//        );

        User roman = userService.findUserById(1L);
        bankAccountService.addNewBankAccountToUser(roman,
                BankAccount.builder()
                        .name("Forte")
                        .balance(new BigDecimal(1939))
                        .userId(roman.getId())
                        .build()
        );
        costCategoryService.addNewCostCategoryToUser(roman,
                CostCategory.builder()
                        .name("Study")
                        .categoryType(CostCategory.CostCategoryType.EXPENSE)
                        .userId(roman.getId())
                        .build()
        );

        getUserInfo(roman);

        System.out.println("---------------------------------------");

        User alex = userService.findUserById(2L);

        getUserInfo(alex);

        operationService.createOperation(roman.getBankAccounts().get(0), roman.getCostCategories().get(1), new BigDecimal(515));
        operationService.createOperation(roman.getBankAccounts().get(1), roman.getCostCategories().get(2), new BigDecimal(1000));
        operationService.createOperation(roman.getBankAccounts().get(0), roman.getCostCategories().get(1), new BigDecimal(8866));

        System.out.println("Все операции: ");
        List<Operation> allOperations = operationService.findAllOperations();
        allOperations.forEach(System.out::println);
        System.out.println();

        System.out.println("Все операции пользователя " + roman.getName());
        List<Operation> romanOperations = operationService.findAllOperationsByUser(roman);
        romanOperations.forEach(System.out::println);
        System.out.println("Расходы - " + operationService.getSum(romanOperations, CostCategory.CostCategoryType.EXPENSE));
        System.out.println("Доходы - " + operationService.getSum(romanOperations, CostCategory.CostCategoryType.INCOME));
        System.out.println();

        System.out.println("Все операции пользователя " + roman.getName() + " по категории " + roman.getCostCategories().get(1).getName());
        List<Operation> romanCostCategoryOperations = operationService.findAllOperationsByCostCategory(roman.getCostCategories().get(1));
        romanCostCategoryOperations.forEach(System.out::println);
        System.out.println("Сумма - " + operationService.getSum(romanCostCategoryOperations, roman.getCostCategories().get(1).getCategoryType()));
        System.out.println();

        System.out.println("Все операции пользователя " + roman.getName() + " на 19.10.2024: ");
        List<Operation> romanDateOperations = operationService.findAllOperationsForDate(LocalDate.of(2024, 10, 19));
        romanDateOperations.forEach(System.out::println);
        System.out.println("Расходы - " + operationService.getSum(romanDateOperations, CostCategory.CostCategoryType.EXPENSE));
        System.out.println("Доходы - " + operationService.getSum(romanDateOperations, CostCategory.CostCategoryType.INCOME));
        System.out.println();

        System.out.println("Все операции пользователя " + roman.getName() + " c 03.10.2024 по 13.10.2024: ");
        List<Operation> romanDatesOperations = operationService.findAllOperationsBetweenDates(LocalDate.of(2024, 10, 3), LocalDate.of(2024, 10, 13));
        romanDatesOperations.forEach(System.out::println);
        System.out.println("Расходы - " + operationService.getSum(romanDatesOperations, CostCategory.CostCategoryType.EXPENSE));
        System.out.println("Доходы - " + operationService.getSum(romanDatesOperations, CostCategory.CostCategoryType.INCOME));
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
}
