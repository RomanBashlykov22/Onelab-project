package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.entities.BankAccount;
import kz.romanb.onelabproject.entities.CostCategory;
import kz.romanb.onelabproject.entities.Operation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface OperationRepository extends JpaRepository<Operation, Long> {
    List<Operation> findAllByBankAccount(BankAccount bankAccount);
    List<Operation> findAllByCostCategory(CostCategory costCategory);
    List<Operation> findAllByDate(LocalDate date);
    List<Operation> findAllByDateBetween(LocalDate start, LocalDate end);
}
