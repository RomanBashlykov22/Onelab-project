package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.models.entities.BankAccount;
import kz.romanb.onelabproject.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findAllByUserId(Long userId);

    @Modifying
    @Query("UPDATE BankAccount b SET b.balance = :balance WHERE b.id = :id")
    void changeBalance(Long id, BigDecimal balance);
}
