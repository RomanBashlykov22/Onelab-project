package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.entities.BankAccount;
import kz.romanb.onelabproject.entities.User;
import kz.romanb.onelabproject.exceptions.NotEnoughMoneyException;
import kz.romanb.onelabproject.repositories.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountService {
    private final BankAccountRepository bankAccountRepository;

    public List<BankAccount> getAllUserAccounts(User user){
        List<BankAccount> allUserBankAccounts = bankAccountRepository.findAllUserBankAccounts(user.getId());
        user.setBankAccounts(allUserBankAccounts);
        return allUserBankAccounts;
    }

    public BankAccount addNewBankAccountToUser(User user, BankAccount bankAccount) {
        user.getBankAccounts().stream()
                .filter(b -> b.getName().equals(bankAccount.getName()))
                .findAny()
                .ifPresent(b -> {
                    throw new IllegalArgumentException("У пользователя уже есть такой счет");
                });
        if (bankAccount.getBalance() == null || bankAccount.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new NotEnoughMoneyException("Баланс на счете меньше нуля");
        }
        user.getBankAccounts().add(bankAccount);
        bankAccountRepository.save(bankAccount);
        return bankAccount;
    }

    public void changeBalance(BankAccount bankAccount, BigDecimal newBalance) {
        bankAccountRepository.changeBalance(bankAccount.getId(), newBalance);
        bankAccount.setBalance(newBalance);
    }

    public BankAccount findById(Long id) {
        return bankAccountRepository.findById(id).get();
    }
}
