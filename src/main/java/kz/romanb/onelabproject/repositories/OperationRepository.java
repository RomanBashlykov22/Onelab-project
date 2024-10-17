package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.entities.BankAccount;
import kz.romanb.onelabproject.entities.CostCategory;
import kz.romanb.onelabproject.entities.Operation;
import kz.romanb.onelabproject.entities.User;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface OperationRepository extends Repository<Operation, Long> {
    AtomicInteger operationId = new AtomicInteger(0);

    List<Operation> findAllOperationsByUser(User user);

    List<Operation> findAllOperationsByBankAccount(BankAccount bankAccount);

    List<Operation> findAllOperationsByCostCategory(CostCategory costCategory);

    List<Operation> findAllOperationsByCostCategoryType(CostCategory.CostCategoryType categoryType);

    List<Operation> findAllOperationsForDate(LocalDate date);

    List<Operation> findAllOperationsBetweenDates(LocalDate startDate, LocalDate endDate);
}
