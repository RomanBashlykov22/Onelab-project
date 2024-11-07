package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.models.dto.OperationDto;
import kz.romanb.onelabproject.models.dto.SumResponse;
import kz.romanb.onelabproject.models.entities.BankAccount;
import kz.romanb.onelabproject.models.entities.CostCategory;
import kz.romanb.onelabproject.models.entities.Operation;
import kz.romanb.onelabproject.models.entities.User;
import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.exceptions.NotEnoughMoneyException;
import kz.romanb.onelabproject.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OperationService {
    public static final String OPERATION_WITH_ID_DOES_NOT_EXISTS = "Операция с id %d не существует";

    private final OperationRepository operationRepository;
    private final BankAccountService bankAccountService;
    private final CostCategoryService costCategoryService;
    private final UserService userService;

    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public Operation createOperation(Long bankAccountId, Long costCategoryId, BigDecimal amount) {
        Optional<BankAccount> bankAccountOptional = bankAccountService.findById(bankAccountId);
        if (bankAccountOptional.isEmpty()) {
            throw new DBRecordNotFoundException(String.format(BankAccountService.BANK_ACCOUNT_WITH_ID_DOES_NOT_EXISTS, bankAccountId));
        }

        Optional<CostCategory> costCategoryOptional = costCategoryService.findById(costCategoryId);
        if (costCategoryOptional.isEmpty()) {
            throw new DBRecordNotFoundException(String.format(CostCategoryService.COST_CATEGORY_WITH_ID_DOES_NOT_EXISTS, costCategoryId));
        }

        BankAccount bankAccount = bankAccountOptional.get();
        CostCategory costCategory = costCategoryOptional.get();

        BigDecimal newBalance = bankAccount.getBalance();
        if (costCategory.getCategoryType().equals(CostCategory.CostCategoryType.EXPENSE)) {
            newBalance = newBalance.subtract(amount);
        } else {
            newBalance = newBalance.add(amount);
        }
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new NotEnoughMoneyException("На счете не достаточно средств");
        }

        bankAccountService.changeBalance(bankAccountId, newBalance);
        bankAccount.setBalance(newBalance);
        return operationRepository.save(Operation.builder()
                .bankAccount(bankAccount)
                .costCategory(costCategory)
                .amount(amount)
                .date(LocalDate.now())
                .build()
        );
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public Optional<Operation> findOperationById(Long id) {
        return operationRepository.findById(id);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public List<Operation> findAllOperations() {
        return operationRepository.findAll();
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public List<Operation> findAllOperationsByUser(Long userId) {
        Optional<User> userOptional = userService.findUserById(userId);
        if (userOptional.isEmpty()) {
            throw new DBRecordNotFoundException(String.format(UserService.USER_WITH_ID_DOES_NOT_EXISTS, userId));
        }
        List<Operation> operations = new ArrayList<>();
        for (BankAccount b : userOptional.get().getBankAccounts()) {
            operations.addAll(operationRepository.findAllByBankAccount(b));
        }
        return operations.stream()
                .sorted(Comparator.comparing(Operation::getDate).reversed())
                .toList();
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public List<Operation> findAllOperationsByCostCategory(Long costCategoryId) {
        Optional<CostCategory> costCategoryOptional = costCategoryService.findById(costCategoryId);
        if (costCategoryOptional.isEmpty()) {
            throw new DBRecordNotFoundException(String.format(CostCategoryService.COST_CATEGORY_WITH_ID_DOES_NOT_EXISTS, costCategoryId));
        }
        List<Operation> operations = operationRepository.findAllByCostCategory(costCategoryOptional.get());
        return operations.stream()
                .sorted(Comparator.comparing(Operation::getDate).reversed())
                .toList();
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public List<Operation> findAllOperationsForDate(LocalDate date) {
        return operationRepository.findAllByDate(date);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public List<Operation> findAllOperationsBetweenDates(LocalDate startDate, LocalDate endDate) {
        List<Operation> operations = operationRepository.findAllByDateBetween(startDate, endDate);
        return operations.stream()
                .sorted(Comparator.comparing(Operation::getDate).reversed())
                .toList();
    }


    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public SumResponse getSum(List<OperationDto> operations) {
        return SumResponse.builder()
                .amountOfOperations(operations.size())
                .expense(
                        operations.stream()
                                .filter(o -> o.getCostCategoryDto().getCategoryType().equals(CostCategory.CostCategoryType.EXPENSE))
                                .map(OperationDto::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                )
                .income(
                        operations.stream()
                                .filter(o -> o.getCostCategoryDto().getCategoryType().equals(CostCategory.CostCategoryType.INCOME))
                                .map(OperationDto::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                ).build();
    }
}
