package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.models.entities.BankAccount;
import kz.romanb.onelabproject.models.entities.CostCategory;
import kz.romanb.onelabproject.models.entities.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Long> {
    List<Operation> findAllByBankAccount(BankAccount bankAccount);

    List<Operation> findAllByCostCategory(CostCategory costCategory);

    List<Operation> findAllByDate(LocalDate date);

    List<Operation> findAllByDateBetween(LocalDate start, LocalDate end);
}
