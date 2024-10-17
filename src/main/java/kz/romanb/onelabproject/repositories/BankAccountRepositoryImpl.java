package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.entities.BankAccount;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class BankAccountRepositoryImpl implements BankAccountRepository {
    public static final List<BankAccount> bankAccounts = new ArrayList<>();

    @Override
    public BankAccount save(BankAccount bankAccount) {
        Optional<BankAccount> bankAccountOptional = findById(bankAccount.getId());
        bankAccountOptional.ifPresent(bankAccounts::remove);
        bankAccounts.add(bankAccount);
        sortBankAccounts();
        return bankAccount;
    }

    @Override
    public Optional<BankAccount> findById(Long id) {
        return bankAccounts.stream().filter(u -> u.getId() == id).findFirst();
    }

    @Override
    public List<BankAccount> findAll() {
        return bankAccounts;
    }

    private static void sortBankAccounts() {
        bankAccounts.sort(Comparator.comparing(BankAccount::getId));
    }
}
