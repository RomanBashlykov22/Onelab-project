package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.entities.BankAccount;
import kz.romanb.onelabproject.entities.CostCategory;
import kz.romanb.onelabproject.entities.Operation;

import java.time.LocalDate;
import java.util.List;

public interface OperationRepository extends Repository<Operation, Long> {

    List<Operation> findAllOperationsByBankAccount(BankAccount bankAccount);

    List<Operation> findAllOperationsByCostCategory(CostCategory costCategory);

    List<Operation> findAllOperationsForDate(LocalDate date);

    List<Operation> findAllOperationsBetweenDates(LocalDate startDate, LocalDate endDate);
}
