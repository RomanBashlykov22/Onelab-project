package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.entities.BankAccount;
import kz.romanb.onelabproject.entities.CostCategory;
import kz.romanb.onelabproject.entities.Operation;
import kz.romanb.onelabproject.entities.User;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class OperationRepositoryImpl implements OperationRepository {
    public static final List<Operation> operations = new ArrayList<>();

    @Override
    public Operation save(Operation operation) {
        Optional<Operation> operationOptional = findById(operation.getId());
        operationOptional.ifPresent(operations::remove);
        operations.add(operation);
        return operation;
    }

    @Override
    public Optional<Operation> findById(Long id) {
        return operations.stream().filter(o -> o.getId() == id).findFirst();
    }

    @Override
    public List<Operation> findAll() {
        return operations;
    }

    @Override
    public List<Operation> findAllOperationsByUser(User user) {
        return operations.stream()
                .filter(o -> user.getBankAccounts().contains(o.getBankAccount()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Operation> findAllOperationsByBankAccount(BankAccount bankAccount) {
        return operations.stream()
                .filter(o -> o.getBankAccount().getId().equals(bankAccount.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Operation> findAllOperationsByCostCategory(CostCategory costCategory) {
        return operations.stream()
                .filter(o -> o.getCostCategory().getId().equals(costCategory.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Operation> findAllOperationsByCostCategoryType(CostCategory.CostCategoryType categoryType) {
        return operations.stream()
                .filter(o -> o.getCostCategory().getCategoryType() == categoryType)
                .collect(Collectors.toList());
    }

    @Override
    public List<Operation> findAllOperationsForDate(LocalDate date) {
        return operations.stream()
                .filter(o -> o.getDate().isEqual(date))
                .collect(Collectors.toList());
    }

    @Override
    public List<Operation> findAllOperationsBetweenDates(LocalDate startDate, LocalDate endDate) {
        return operations.stream()
                .filter(o -> !o.getDate().isBefore(startDate) && !o.getDate().isAfter(endDate))
                .collect(Collectors.toList());
    }
}
