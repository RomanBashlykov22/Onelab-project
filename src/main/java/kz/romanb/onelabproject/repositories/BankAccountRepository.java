package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.entities.BankAccount;
import kz.romanb.onelabproject.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findAllByUser(User user);

    @Modifying
    @Query("UPDATE BankAccount b SET b.balance = :balance WHERE b.id = :id")
    void changeBalance(Long id, BigDecimal balance);
}
