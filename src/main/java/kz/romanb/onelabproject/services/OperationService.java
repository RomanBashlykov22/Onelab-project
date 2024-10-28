package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.entities.BankAccount;
import kz.romanb.onelabproject.entities.CostCategory;
import kz.romanb.onelabproject.entities.Operation;
import kz.romanb.onelabproject.entities.User;
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
    private final OperationRepository operationRepository;
    private final BankAccountService bankAccountService;

    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
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

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public Optional<Operation> findOperationById(Long id) {
        Optional<Operation> operationOptional = operationRepository.findById(id);
        if(operationOptional.isEmpty()){
            throw new DBRecordNotFoundException("Операция с id " + id + " не найдена");
        }
        return operationOptional;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public List<Operation> findAllOperations() {
        return operationRepository.findAll();
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public List<Operation> findAllOperationsByUser(User user) {
        List<Operation> operations = new ArrayList<>();
        for (BankAccount b: user.getBankAccounts()) {
            operations.addAll(operationRepository.findAllByBankAccount(b));
        }
        return operations.stream()
                .sorted(Comparator.comparing(Operation::getDate).reversed())
                .toList();
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public List<Operation> findAllOperationsByCostCategory(CostCategory costCategory) {
        List<Operation> operations = operationRepository.findAllByCostCategory(costCategory);
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
    public BigDecimal getSum(List<Operation> operations, CostCategory.CostCategoryType categoryType) {
        return operations.stream()
                .filter(o -> o.getCostCategory().getCategoryType().equals(categoryType))
                .map(Operation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
