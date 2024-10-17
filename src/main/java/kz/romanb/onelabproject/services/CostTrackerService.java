package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.entities.BankAccount;
import kz.romanb.onelabproject.entities.CostCategory;
import kz.romanb.onelabproject.entities.Operation;
import kz.romanb.onelabproject.entities.User;
import kz.romanb.onelabproject.exceptions.NotEnoughMoneyException;
import kz.romanb.onelabproject.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CostTrackerService {
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CostCategoryRepository costCategoryRepository;
    private final OperationRepository operationRepository;

    public User createNewUser(User user) {
        return userRepository.save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public BankAccount addNewBankAccountToUser(User user, BankAccount bankAccount) {
        Optional<BankAccount> bankAccountOptional = bankAccountRepository.findById(bankAccount.getId());
        bankAccountOptional
                .ifPresent(b -> {
                    throw new IllegalArgumentException("Счет с id " + b.getId() + " уже существует");
                });
        user.getBankAccounts().stream()
                .filter(b -> Objects.equals(b.getId(), bankAccount.getId()) || b.getName().equals(bankAccount.getName()))
                .findAny()
                .ifPresent(b -> {
                    throw new IllegalArgumentException("У пользователя уже есть такой счет");
                });
        if (bankAccount.getBalance() == null || bankAccount.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new NotEnoughMoneyException("Баланс на счете меньше нуля");
        }
        user.getBankAccounts().add(bankAccount);
        bankAccountRepository.save(bankAccount);
        return bankAccount;
    }

    public CostCategory addNewCostCategoryToUser(User user, CostCategory costCategory) {
        Optional<CostCategory> costCategoryOptional = costCategoryRepository.findById(costCategory.getId());
        costCategoryOptional
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Категория с id " + costCategory.getId() + " уже существует");
                });
        user.getCostCategories().add(costCategory);
        costCategoryRepository.save(costCategory);
        return costCategory;
    }

    public Operation createOperation(BankAccount bankAccount, CostCategory costCategory, BigDecimal amount) {
        if (costCategory.getCategoryType().equals(CostCategory.CostCategoryType.EXPENSE)) {
            createExpenseOperation(bankAccount, amount);
        } else {
            createIncomeOperation(bankAccount, amount);
        }
        Operation operation = Operation.builder()
                .id(OperationRepository.operationId.incrementAndGet())
                .costCategory(costCategory)
                .bankAccount(bankAccount)
                .amount(amount)
                .build();
        operationRepository.save(operation);
        return operation;
    }

    private void createIncomeOperation(BankAccount bankAccount, BigDecimal amount) {
        bankAccount.setBalance(bankAccount.getBalance().add(amount));
        if (bankAccount.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            bankAccount.setBalance(bankAccount.getBalance().subtract(amount));
            throw new NotEnoughMoneyException("На счете не достаточно средств");
        }
    }

    private void createExpenseOperation(BankAccount bankAccount, BigDecimal amount) {
        if (bankAccount.getBalance().compareTo(amount) < 0) {
            throw new NotEnoughMoneyException("На счете не достаточно средств");
        }
        bankAccount.setBalance(bankAccount.getBalance().subtract(amount));
        if (bankAccount.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            bankAccount.setBalance(bankAccount.getBalance().add(amount));
            throw new NotEnoughMoneyException("На счете не достаточно средств");
        }
    }

    public Optional<Operation> findOperationById(Long id) {
        return operationRepository.findById(id);
    }

    public List<Operation> findAllOperations() {
        return operationRepository.findAll().stream()
                .sorted(Comparator.comparing(o -> o.getBankAccount().getId()))
                .collect(Collectors.toList());
    }

    public List<Operation> findAllOperationsByUser(User user) {
        return operationRepository.findAllOperationsByUser(user).stream()
                .sorted(Comparator.comparing(Operation::getDate).reversed())
                .collect(Collectors.toList());
    }

    public List<Operation> findAllOperationsByCostCategory(CostCategory costCategory) {
        return operationRepository.findAllOperationsByCostCategory(costCategory).stream()
                .sorted(Comparator.comparing(Operation::getDate).reversed())
                .collect(Collectors.toList());
    }

    public List<Operation> findAllOperationsForDate(LocalDate date) {
        return operationRepository.findAllOperationsForDate(date).stream()
                .sorted(Comparator.comparing(Operation::getDate).reversed())
                .collect(Collectors.toList());
    }

    public List<Operation> findAllOperationsBetweenDates(LocalDate startDate, LocalDate endDate) {
        return operationRepository.findAllOperationsBetweenDates(startDate, endDate).stream()
                .sorted(Comparator.comparing(Operation::getDate).reversed())
                .collect(Collectors.toList());
    }


    public BigDecimal getSum(List<Operation> operations, CostCategory.CostCategoryType categoryType) {
        return operations.stream()
                .filter(o -> o.getCostCategory().getCategoryType().equals(categoryType))
                .map(Operation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
