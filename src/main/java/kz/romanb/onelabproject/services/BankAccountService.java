package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.models.dto.BankAccountDto;
import kz.romanb.onelabproject.models.entities.BankAccount;
import kz.romanb.onelabproject.models.entities.User;
import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.exceptions.NotEnoughMoneyException;
import kz.romanb.onelabproject.repositories.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BankAccountService {
    public static final String BANK_ACCOUNT_WITH_ID_DOES_NOT_EXISTS = "Счета с id %d не существует";

    private final BankAccountRepository bankAccountRepository;
    private final UserService userService;

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public List<BankAccount> getAllUserAccounts(Long userId) {
        if (userService.findUserById(userId).isEmpty()) {
            throw new DBRecordNotFoundException(String.format(UserService.USER_WITH_ID_DOES_NOT_EXISTS, userId));
        }
        return bankAccountRepository.findAllByUserId(userId);
    }

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
    public BankAccount addNewBankAccountToUser(Long userId, BankAccountDto bankAccountDto) {
        Optional<User> userOptional = userService.findUserById(userId);
        if (userOptional.isEmpty()) {
            throw new DBRecordNotFoundException(String.format(UserService.USER_WITH_ID_DOES_NOT_EXISTS, userId));
        }
        bankAccountRepository.findAllByUserId(userId).stream()
                .filter(b -> b.getName().equals(bankAccountDto.getName()))
                .findAny()
                .ifPresent(b -> {
                    throw new IllegalArgumentException("У пользователя уже есть такой счет");
                });
        if (bankAccountDto.getBalance() == null || bankAccountDto.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new NotEnoughMoneyException("Баланс на счете меньше нуля");
        }
        BankAccount bankAccount = BankAccount.builder()
                .user(userOptional.get())
                .balance(bankAccountDto.getBalance())
                .name(bankAccountDto.getName())
                .build();
        return bankAccountRepository.save(bankAccount);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public String changeBalance(Long bankAccountId, BigDecimal newBalance) {
        Optional<BankAccount> bankAccountOptional = bankAccountRepository.findById(bankAccountId);
        if (bankAccountOptional.isEmpty()) {
            throw new DBRecordNotFoundException(String.format(BANK_ACCOUNT_WITH_ID_DOES_NOT_EXISTS, bankAccountId));
        }
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new NotEnoughMoneyException("Новый баланс меньше нуля");
        }
        bankAccountRepository.changeBalance(bankAccountOptional.get().getId(), newBalance);
        return "Баланс изменен";
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public Optional<BankAccount> findById(Long id) {
        return bankAccountRepository.findById(id);
    }
}
