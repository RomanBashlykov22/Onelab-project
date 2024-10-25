package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.entities.BankAccount;
import kz.romanb.onelabproject.entities.User;
import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.exceptions.NotEnoughMoneyException;
import kz.romanb.onelabproject.repositories.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BankAccountServiceTest {
    @Mock
    BankAccountRepository bankAccountRepository;
    @InjectMocks
    BankAccountService bankAccountService;

    User user = null;
    BankAccount bankAccount1 = null;
    BankAccount bankAccount2 = null;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bankAccount1 = BankAccount.builder()
                .id(1L)
                .name("Account1")
                .balance(new BigDecimal(1000))
                .build();
        bankAccount2 = BankAccount.builder()
                .id(2L)
                .name("Account2")
                .balance(new BigDecimal(3000))
                .build();
        user = User.builder()
                .id(1L)
                .name("Username")
                .build();
        user.getBankAccounts().add(bankAccount1);
        user.getBankAccounts().add(bankAccount2);
        bankAccount1.setUser(user);
        bankAccount2.setUser(user);
    }

    @Test
    void testGetAllUserAccounts() {
        when(bankAccountRepository.findAllByUser(user)).thenReturn(user.getBankAccounts());
        List<BankAccount> bankAccounts = bankAccountService.getAllUserAccounts(user);
        assertNotNull(bankAccounts);
        assertEquals(bankAccounts.size(), user.getBankAccounts().size());
        verify(bankAccountRepository, times(1)).findAllByUser(user);
    }

    @Test
    void testAddNewBankAccountToUserWhenBankAccountWithNameAlreadyExists() {
        BankAccount bankAccountWithNameAlreadyExists = BankAccount.builder().name("Account1").build();
        assertThrows(IllegalArgumentException.class, () -> bankAccountService.addNewBankAccountToUser(user, bankAccountWithNameAlreadyExists));
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void testAddNewBankAccountToUserWhenBankAccountWhenBalanceIsNull() {
        BankAccount bankAccountWithBalanceIsNull = BankAccount.builder().name("Account3").balance(null).build();
        assertThrows(NotEnoughMoneyException.class, () -> bankAccountService.addNewBankAccountToUser(user, bankAccountWithBalanceIsNull));
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void testAddNewBankAccountToUserWhenBankAccountWhenBalanceIsLessThanZero() {
        BankAccount bankAccountWithBalanceIsLessThanZero = BankAccount.builder().name("Account3").balance(new BigDecimal(-1)).build();
        assertThrows(NotEnoughMoneyException.class, () -> bankAccountService.addNewBankAccountToUser(user, bankAccountWithBalanceIsLessThanZero));
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void testAddNewBankAccountToUserWhenBankAccountWhenBalanceIsEqualsZero() {
        BankAccount bankAccountWithBalanceIsEqualsZero = BankAccount.builder().name("Account3").balance(new BigDecimal(0)).build();
        when(bankAccountRepository.save(bankAccountWithBalanceIsEqualsZero)).thenReturn(BankAccount.builder().id(3L).name("Account3").balance(new BigDecimal(0)).build());
        BankAccount result = bankAccountService.addNewBankAccountToUser(user, bankAccountWithBalanceIsEqualsZero);
        assertTrue(user.getBankAccounts().stream().anyMatch(b -> b.getId().equals(3L)));
        assertEquals(result.getBalance(), bankAccountWithBalanceIsEqualsZero.getBalance());
        assertEquals(0, result.getBalance().longValue());
        verify(bankAccountRepository, times(1)).save(bankAccountWithBalanceIsEqualsZero);
    }

    @Test
    void testAddNewBankAccountToUserWhenBankAccountWhenBalanceIsGreaterThanZero() {
        BankAccount bankAccountWithBalanceIsGreaterThanZero = BankAccount.builder().name("Account3").balance(new BigDecimal(1000)).build();
        when(bankAccountRepository.save(bankAccountWithBalanceIsGreaterThanZero)).thenReturn(BankAccount.builder().id(3L).name("Account3").balance(new BigDecimal(1000)).build());
        BankAccount result = bankAccountService.addNewBankAccountToUser(user, bankAccountWithBalanceIsGreaterThanZero);
        assertTrue(user.getBankAccounts().stream().anyMatch(b -> b.getId().equals(3L)));
        assertEquals(result.getBalance(), bankAccountWithBalanceIsGreaterThanZero.getBalance());
        assertEquals(1000, result.getBalance().longValue());
        verify(bankAccountRepository, times(1)).save(bankAccountWithBalanceIsGreaterThanZero);
    }

    @Test
    void testChangeBalanceWhenNewBalanceIsLessThanZero() {
        BankAccount bankAccount = user.getBankAccounts().get(0);
        assertThrows(NotEnoughMoneyException.class, () -> bankAccountService.changeBalance(bankAccount, new BigDecimal(-1)));
        assertEquals(0, bankAccount.getBalance().compareTo(new BigDecimal(1000)));
        verify(bankAccountRepository, never()).changeBalance(any(), any());
    }

    @Test
    void testChangeBalanceWhenNewBalanceIsEqualsZero() {
        BankAccount bankAccount = user.getBankAccounts().get(0);
        doNothing().when(bankAccountRepository).changeBalance(1L, new BigDecimal(0));
        bankAccountService.changeBalance(bankAccount, new BigDecimal(0));
        verify(bankAccountRepository, times(1)).changeBalance(bankAccount.getId(), new BigDecimal(0));
    }

    @Test
    void testChangeBalanceWhenNewBalanceIsGreaterThanZero() {
        BankAccount bankAccount = user.getBankAccounts().get(0);
        doNothing().when(bankAccountRepository).changeBalance(1L, new BigDecimal(500));
        bankAccountService.changeBalance(bankAccount, new BigDecimal(500));
        verify(bankAccountRepository, times(1)).changeBalance(bankAccount.getId(), new BigDecimal(500));
    }

    @Test
    void testFindByIdWhenBankAccountExists() {
        Long bankAccountId = 1L;
        when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(bankAccount1));
        Optional<BankAccount> result = bankAccountService.findById(bankAccountId);
        assertTrue(result.isPresent());
        assertEquals(bankAccountId, result.get().getId());
        assertEquals(result.get().getName(), bankAccount1.getName());
        verify(bankAccountRepository, times(1)).findById(bankAccountId);
    }

    @Test
    void testFindByIdWhenBankAccountDoesNotExists() {
        Long bankAccountId = 1L;
        when(bankAccountRepository.findById(bankAccountId)).thenThrow(new DBRecordNotFoundException("Счет не существует"));
        assertThrows(DBRecordNotFoundException.class, () -> bankAccountService.findById(bankAccountId));
        verify(bankAccountRepository, times(1)).findById(bankAccountId);
    }
}