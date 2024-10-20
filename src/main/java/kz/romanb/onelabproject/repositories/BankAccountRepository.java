package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.entities.BankAccount;

import java.math.BigDecimal;
import java.util.List;

public interface BankAccountRepository extends Repository<BankAccount, Long> {
    List<BankAccount> findAllUserBankAccounts(Long userId);

    void changeBalance(Long id, BigDecimal balance);
}
