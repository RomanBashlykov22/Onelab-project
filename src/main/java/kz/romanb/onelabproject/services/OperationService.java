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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OperationService {
    private final OperationRepository operationRepository;
    private final BankAccountService bankAccountService;
    private final CostCategoryService costCategoryService;

    public void createOperation(BankAccount bankAccount, CostCategory costCategory, BigDecimal amount) {
        BigDecimal newBalance = bankAccount.getBalance();
        if (costCategory.getCategoryType().equals(CostCategory.CostCategoryType.EXPENSE)) {
            newBalance = newBalance.subtract(amount);
        } else {
            newBalance = newBalance.add(amount);
        }
        if(newBalance.compareTo(BigDecimal.ZERO) < 0){
            throw new NotEnoughMoneyException("На счете не достаточно средств");
        }
        bankAccountService.changeBalance(bankAccount, newBalance);
        operationRepository.save(Operation.builder()
                .bankAccount(bankAccount)
                .costCategory(costCategory)
                .amount(amount)
                .date(LocalDate.now())
                .build()
        );
    }

    public Optional<Operation> findOperationById(Long id) {
        return operationRepository.findById(id);
    }

    public List<Operation> findAllOperations() {
        List<Operation> operations = operationRepository.findAll();
        operations.forEach(o -> {
            o.setBankAccount(bankAccountService.findById(o.getBankAccount().getId()));
            o.setCostCategory(costCategoryService.findById(o.getCostCategory().getId()));
        });
        return operations;
    }

    public List<Operation> findAllOperationsByUser(User user) {
        List<Operation> operations = new ArrayList<>();
        for (BankAccount b: user.getBankAccounts()) {
            operations.addAll(operationRepository.findAllOperationsByBankAccount(b));
        }
        operations.forEach(o -> {
            o.setBankAccount(bankAccountService.findById(o.getBankAccount().getId()));
            o.setCostCategory(costCategoryService.findById(o.getCostCategory().getId()));
        });
        return operations.stream()
                .sorted(Comparator.comparing(Operation::getDate).reversed())
                .collect(Collectors.toList());
    }

    public List<Operation> findAllOperationsByCostCategory(CostCategory costCategory) {
        List<Operation> operations = operationRepository.findAllOperationsByCostCategory(costCategory);
        operations.forEach(o -> {
            o.setBankAccount(bankAccountService.findById(o.getBankAccount().getId()));
            o.setCostCategory(costCategoryService.findById(o.getCostCategory().getId()));
        });
        return operations.stream()
                .sorted(Comparator.comparing(Operation::getDate).reversed())
                .collect(Collectors.toList());
    }

    public List<Operation> findAllOperationsForDate(LocalDate date) {
        List<Operation> operations = operationRepository.findAllOperationsForDate(date);
        operations.forEach(o -> {
            o.setBankAccount(bankAccountService.findById(o.getBankAccount().getId()));
            o.setCostCategory(costCategoryService.findById(o.getCostCategory().getId()));
        });
        return operations.stream()
                .sorted(Comparator.comparing(Operation::getDate).reversed())
                .collect(Collectors.toList());
    }

    public List<Operation> findAllOperationsBetweenDates(LocalDate startDate, LocalDate endDate) {
        List<Operation> operations = operationRepository.findAllOperationsBetweenDates(startDate, endDate);
        operations.forEach(o -> {
            o.setBankAccount(bankAccountService.findById(o.getBankAccount().getId()));
            o.setCostCategory(costCategoryService.findById(o.getCostCategory().getId()));
        });
        return operations.stream()
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
