package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.models.dto.BankAccountDto;
import kz.romanb.onelabproject.models.entities.BankAccount;
import kz.romanb.onelabproject.models.entities.User;
import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.exceptions.NotEnoughMoneyException;
import kz.romanb.onelabproject.kafka.KafkaService;
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
    @Mock
    KafkaService kafkaService;
    @Mock
    UserService userService;
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
                .username("Username")
                .build();
        user.getBankAccounts().add(bankAccount1);
        user.getBankAccounts().add(bankAccount2);
        bankAccount1.setUser(user);
        bankAccount2.setUser(user);
    }

    @Test
    void testGetAllUserAccounts() {
        when(bankAccountRepository.findAllByUserId(1L)).thenReturn(user.getBankAccounts());

        List<BankAccount> bankAccounts = bankAccountService.getAllUserAccounts(1L);

        assertNotNull(bankAccounts);
        assertEquals(bankAccounts.size(), user.getBankAccounts().size());
        verify(bankAccountRepository, times(1)).findAllByUserId(1L);
    }

    @Test
    void testAddNewBankAccountToUserWhenUserDoesNotExists(){
        when(userService.findUserById(1L)).thenReturn(Optional.empty());

        assertThrows(DBRecordNotFoundException.class, () -> bankAccountService.addNewBankAccountToUser(1L, BankAccountDto.builder().build()));

        verify(bankAccountRepository, never()).findAllByUserId(any());
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void testAddNewBankAccountToUserWhenBankAccountWithNameAlreadyExists() {
        BankAccountDto bankAccountWithNameAlreadyExists = BankAccountDto.builder().name("Account1").build();
        when(userService.findUserById(1L)).thenReturn(Optional.of(user));
        when(bankAccountRepository.findAllByUserId(1L)).thenReturn(user.getBankAccounts());

        assertThrows(IllegalArgumentException.class, () -> bankAccountService.addNewBankAccountToUser(1L, bankAccountWithNameAlreadyExists));

        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void testAddNewBankAccountToUserWhenBankAccountWhenBalanceIsNull() {
        BankAccountDto bankAccountWithBalanceIsNull = BankAccountDto.builder().name("Account3").balance(null).build();
        when(userService.findUserById(1L)).thenReturn(Optional.of(user));
        when(bankAccountRepository.findAllByUserId(1L)).thenReturn(user.getBankAccounts());

        assertThrows(NotEnoughMoneyException.class, () -> bankAccountService.addNewBankAccountToUser(1L, bankAccountWithBalanceIsNull));

        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void testAddNewBankAccountToUserWhenBankAccountWhenBalanceIsLessThanZero() {
        BankAccountDto bankAccountWithBalanceIsLessThanZero = BankAccountDto.builder().name("Account3").balance(new BigDecimal(-1)).build();
        when(userService.findUserById(1L)).thenReturn(Optional.of(user));
        when(bankAccountRepository.findAllByUserId(1L)).thenReturn(user.getBankAccounts());

        assertThrows(NotEnoughMoneyException.class, () -> bankAccountService.addNewBankAccountToUser(1L, bankAccountWithBalanceIsLessThanZero));

        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void testAddNewBankAccountToUserWhenBankAccountWhenBalanceIsEqualsZero() {
        BankAccountDto bankAccountWithBalanceIsEqualsZero = BankAccountDto.builder().name("Account3").balance(new BigDecimal(0)).build();
        when(userService.findUserById(1L)).thenReturn(Optional.of(user));
        when(bankAccountRepository.findAllByUserId(1L)).thenReturn(user.getBankAccounts());
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(BankAccount.builder().id(3L).name("Account3").balance(new BigDecimal(0)).build());

        BankAccount result = bankAccountService.addNewBankAccountToUser(1L, bankAccountWithBalanceIsEqualsZero);

        assertEquals(result.getBalance(), bankAccountWithBalanceIsEqualsZero.getBalance());
        assertEquals(0, result.getBalance().longValue());
        verify(bankAccountRepository, times(1)).save(any(BankAccount.class));
    }

    @Test
    void testAddNewBankAccountToUserWhenBankAccountWhenBalanceIsGreaterThanZero() {
        BankAccountDto bankAccountWithBalanceIsGreaterThanZero = BankAccountDto.builder().name("Account3").balance(new BigDecimal(1000)).build();
        when(userService.findUserById(1L)).thenReturn(Optional.of(user));
        when(bankAccountRepository.findAllByUserId(1L)).thenReturn(user.getBankAccounts());
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(BankAccount.builder().id(3L).name("Account3").balance(new BigDecimal(1000)).build());

        BankAccount result = bankAccountService.addNewBankAccountToUser(1L, bankAccountWithBalanceIsGreaterThanZero);

        assertEquals(result.getBalance(), bankAccountWithBalanceIsGreaterThanZero.getBalance());
        assertEquals(1000, result.getBalance().longValue());
        verify(bankAccountRepository, times(1)).save(any(BankAccount.class));
    }

    @Test
    void testChangeBalanceWhenBankAccountDoesNotExists(){
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DBRecordNotFoundException.class, () -> bankAccountService.changeBalance(1L, new BigDecimal(1)));
        verify(bankAccountRepository, never()).changeBalance(any(), any());
    }

    @Test
    void testChangeBalanceWhenNewBalanceIsLessThanZero() {
        BankAccount bankAccount = user.getBankAccounts().get(0);
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccount));

        assertThrows(NotEnoughMoneyException.class, () -> bankAccountService.changeBalance(bankAccount.getId(), new BigDecimal(-1)));
        assertEquals(0, bankAccount.getBalance().compareTo(new BigDecimal(1000)));
        verify(bankAccountRepository, never()).changeBalance(any(), any());
    }

    @Test
    void testChangeBalanceWhenNewBalanceIsEqualsZero() {
        BankAccount bankAccount = user.getBankAccounts().get(0);
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccount));
        doNothing().when(bankAccountRepository).changeBalance(1L, new BigDecimal(0));

        String result = bankAccountService.changeBalance(1L, new BigDecimal(0));

        assertNotNull(result);
        verify(bankAccountRepository, times(1)).changeBalance(bankAccount.getId(), new BigDecimal(0));
    }

    @Test
    void testChangeBalanceWhenNewBalanceIsGreaterThanZero() {
        BankAccount bankAccount = user.getBankAccounts().get(0);
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccount));
        doNothing().when(bankAccountRepository).changeBalance(1L, new BigDecimal(500));

        String result = bankAccountService.changeBalance(1L, new BigDecimal(500));

        assertNotNull(result);
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
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<BankAccount> result = bankAccountService.findById(1L);

        assertTrue(result.isEmpty());
        verify(bankAccountRepository, times(1)).findById(1L);
    }
}