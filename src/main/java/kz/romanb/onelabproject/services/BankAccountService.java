package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.entities.BankAccount;
import kz.romanb.onelabproject.entities.User;
import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.exceptions.NotEnoughMoneyException;
import kz.romanb.onelabproject.kafka.KafkaService;
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
    private final BankAccountRepository bankAccountRepository;
    private final KafkaService kafkaService;

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public List<BankAccount> getAllUserAccounts(User user){
        return bankAccountRepository.findAllByUser(user);
    }

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
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
        BankAccount saved = bankAccountRepository.save(bankAccount);
        kafkaService.sendMessage(user, String.format("Добавление банковского счета %s с начальным балансом %s", saved.getName(), saved.getBalance().toString()));
        user.getBankAccounts().add(saved);
        return saved;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public void changeBalance(BankAccount bankAccount, BigDecimal newBalance) {
        if(newBalance.compareTo(BigDecimal.ZERO) < 0){
            throw new NotEnoughMoneyException("Новый баланс меньше нуля");
        }
        bankAccountRepository.changeBalance(bankAccount.getId(), newBalance);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public Optional<BankAccount> findById(Long id) {
        Optional<BankAccount> bankAccountOptional = bankAccountRepository.findById(id);
        if(bankAccountOptional.isEmpty()){
            throw new DBRecordNotFoundException("Счет с id " + id + " не существует");
        }
        return bankAccountOptional;
    }
}
