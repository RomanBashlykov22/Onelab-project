package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.entities.BankAccount;
import kz.romanb.onelabproject.entities.CostCategory;
import kz.romanb.onelabproject.entities.Operation;
import kz.romanb.onelabproject.entities.User;
import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.exceptions.NotEnoughMoneyException;
import kz.romanb.onelabproject.repositories.OperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class OperationServiceTest {
    @Mock
    OperationRepository operationRepository;
    @Mock
    BankAccountService bankAccountService;
    @InjectMocks
    OperationService operationService;

    User user = null;
    BankAccount bankAccountWithBalance = null;
    BankAccount bankAccountWithoutBalance = null;
    CostCategory expenseCategory = null;
    CostCategory incomeCategory = null;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bankAccountWithBalance = BankAccount.builder()
                .id(1L)
                .name("Account")
                .balance(new BigDecimal(1000))
                .build();
        bankAccountWithoutBalance = BankAccount.builder()
                .id(2L)
                .name("Account")
                .balance(new BigDecimal(-100))
                .build();
        expenseCategory = CostCategory.builder()
                .id(1L)
                .name("Expense")
                .categoryType(CostCategory.CostCategoryType.EXPENSE)
                .build();
        incomeCategory = CostCategory.builder()
                .id(2L)
                .name("Income")
                .categoryType(CostCategory.CostCategoryType.INCOME)
                .build();
        user = User.builder()
                .id(1L)
                .name("Username")
                .bankAccounts(List.of(bankAccountWithBalance, bankAccountWithoutBalance))
                .costCategories(List.of(expenseCategory, incomeCategory))
                .build();
    }

    @Test
    void testCreateIncomeOperation() {
        BigDecimal amount = new BigDecimal(5000);
        BigDecimal expectedBalance = bankAccountWithBalance.getBalance().add(amount);
        operationService.createOperation(bankAccountWithBalance, incomeCategory, amount);
        verify(bankAccountService, times(1)).changeBalance(bankAccountWithBalance, expectedBalance);
        verify(operationRepository).save(argThat(operation ->
                operation.getBankAccount().equals(bankAccountWithBalance) &&
                        operation.getCostCategory().equals(incomeCategory) &&
                        operation.getAmount().equals(amount) &&
                        operation.getDate().equals(LocalDate.now())
        ));
    }

    @Test
    void testCreateExpenseOperationWhenNotEnoughMoney() {
        BigDecimal amount = new BigDecimal(5000);
        assertThrows(NotEnoughMoneyException.class, () -> operationService.createOperation(bankAccountWithBalance, expenseCategory, amount));
        verify(bankAccountService, never()).changeBalance(any(), any());
        verify(operationRepository, never()).save(any());
    }

    @Test
    void testCreateExpenseOperation() {
        BigDecimal amount = new BigDecimal(500);
        BigDecimal expectedBalance = bankAccountWithBalance.getBalance().subtract(amount);
        operationService.createOperation(bankAccountWithBalance, expenseCategory, amount);
        verify(bankAccountService, times(1)).changeBalance(bankAccountWithBalance, expectedBalance);
        verify(operationRepository).save(argThat(operation ->
                operation.getBankAccount().equals(bankAccountWithBalance) &&
                        operation.getCostCategory().equals(expenseCategory) &&
                        operation.getAmount().equals(amount) &&
                        operation.getDate().equals(LocalDate.now())
        ));
    }

//    @Test
//    void testCreateExpenseOperationWhenBalanceIsLessThanZero() {
//        BigDecimal amount = new BigDecimal(5000);
//        assertThrows(NotEnoughMoneyException.class, () -> operationService.createOperation(bankAccountWithoutBalance, expenseCategory, amount));
//        verify(bankAccountService, never()).changeBalance(any(), any());
//        verify(operationRepository, never()).save(any());
//    }

    @Test
    void testFindOperationByIdWhenExists() {
        Long operationId = 1L;
        when(operationRepository.findById(operationId)).thenReturn(Optional.of(Operation.builder().id(1L).build()));
        Optional<Operation> operationOptional = operationService.findOperationById(operationId);
        assertTrue(operationOptional.isPresent());
        verify(operationRepository, times(1)).findById(operationId);
    }

    @Test
    void testFindOperationByIdWhenDoesNotExists() {
        Long operationId = 1L;
        when(operationRepository.findById(operationId)).thenReturn(Optional.empty());
        assertThrows(DBRecordNotFoundException.class, () -> operationService.findOperationById(operationId));
        verify(operationRepository, times(1)).findById(operationId);
    }

    @Test
    void testFindAllOperations() {

    }

    @Test
    void testFindAllOperationsByUser() {
        Operation operation1 = Operation.builder().date(LocalDate.of(2024, 10, 1)).build();
        Operation operation2 = Operation.builder().date(LocalDate.of(2024, 9, 1)).build();
        Operation operation3 = Operation.builder().date(LocalDate.of(2024, 8, 1)).build();
        when(operationRepository.findAllByBankAccount(bankAccountWithBalance)).thenReturn(List.of(operation1, operation2));
        when(operationRepository.findAllByBankAccount(bankAccountWithoutBalance)).thenReturn(List.of(operation3));
        List<Operation> operations = operationService.findAllOperationsByUser(user);
        assertNotNull(operations);
        assertEquals(3, operations.size());
        assertEquals(operation1, operations.get(0));
        assertEquals(operation2, operations.get(1));
        assertEquals(operation3, operations.get(2));
        verify(operationRepository, times(user.getBankAccounts().size())).findAllByBankAccount(any());
    }

    @Test
    void testFindAllOperationsByUserWithoutBankAccounts() {
        user.setBankAccounts(new ArrayList<>());
        List<Operation> operations = operationService.findAllOperationsByUser(user);
        assertTrue(operations.isEmpty());
        verify(operationRepository, never()).findAllByBankAccount(any());
    }

    @Test
    void testFindAllOperationsByUserWithoutOperations() {
        when(operationRepository.findAllByBankAccount(bankAccountWithBalance)).thenReturn(new ArrayList<>());
        when(operationRepository.findAllByBankAccount(bankAccountWithoutBalance)).thenReturn(new ArrayList<>());
        List<Operation> operations = operationService.findAllOperationsByUser(user);
        assertTrue(operations.isEmpty());
        verify(operationRepository, times(user.getBankAccounts().size())).findAllByBankAccount(any());
    }

    @Test
    void testFindAllOperationsByCostCategory() {
        Operation operation1 = Operation.builder().costCategory(expenseCategory).date(LocalDate.of(2024, 10, 1)).build();
        Operation operation2 = Operation.builder().costCategory(expenseCategory).date(LocalDate.of(2024, 9, 1)).build();
        Operation operation3 = Operation.builder().costCategory(incomeCategory).date(LocalDate.of(2024, 8, 1)).build();
        when(operationRepository.findAllByCostCategory(expenseCategory)).thenReturn(List.of(operation1, operation2));
        when(operationRepository.findAllByCostCategory(incomeCategory)).thenReturn(List.of(operation3));
        List<Operation> expenseOperations = operationService.findAllOperationsByCostCategory(expenseCategory);
        assertNotNull(expenseOperations);
        assertEquals(2, expenseOperations.size());
        assertEquals(operation1, expenseOperations.get(0));
        assertEquals(operation2, expenseOperations.get(1));
        verify(operationRepository, times(1)).findAllByCostCategory(expenseCategory);
        List<Operation> incomeOperations = operationService.findAllOperationsByCostCategory(incomeCategory);
        assertNotNull(incomeOperations);
        assertEquals(1, incomeOperations.size());
        assertEquals(operation3, incomeOperations.get(0));
        verify(operationRepository, times(1)).findAllByCostCategory(incomeCategory);
    }

    @Test
    void testFindAllOperationsByCostCategoryWithoutOperations() {
        when(operationRepository.findAllByCostCategory(expenseCategory)).thenReturn(new ArrayList<>());
        List<Operation> operations = operationService.findAllOperationsByCostCategory(expenseCategory);
        assertTrue(operations.isEmpty());
        verify(operationRepository, times(1)).findAllByCostCategory(expenseCategory);
    }

    @Test
    void testFindAllOperationsForDate() {
        LocalDate date = LocalDate.of(2024, 9, 1);
        Operation operation1 = Operation.builder().date(LocalDate.of(2024, 10, 1)).build();
        Operation operation2 = Operation.builder().date(date).build();
        Operation operation3 = Operation.builder().date(date).build();
        when(operationRepository.findAllByDate(date)).thenReturn(List.of(operation2, operation3));
        List<Operation> operations = operationService.findAllOperationsForDate(date);
        assertNotNull(operations);
        assertEquals(2, operations.size());
        assertEquals(operation2, operations.get(0));
        assertEquals(operation3, operations.get(1));
        verify(operationRepository, times(1)).findAllByDate(date);
    }

    @Test
    void testFindAllOperationsBetweenDates() {
        LocalDate start = LocalDate.of(2024, 10, 1);
        LocalDate end = LocalDate.of(2024, 9, 1);
        Operation operation1 = Operation.builder().date(start).build();
        Operation operation2 = Operation.builder().date(end).build();
        Operation operation3 = Operation.builder().date(LocalDate.of(2024, 8, 1)).build();
        when(operationRepository.findAllByDateBetween(start, end)).thenReturn(List.of(operation1, operation2));
        List<Operation> operations = operationService.findAllOperationsBetweenDates(start, end);
        assertNotNull(operations);
        assertEquals(2, operations.size());
        assertEquals(operation1, operations.get(0));
        assertEquals(operation2, operations.get(1));
        verify(operationRepository, times(1)).findAllByDateBetween(start, end);
    }

    @Test
    void testGetSum() {
        Operation operation1 = Operation.builder()
                .id(1L)
                .costCategory(expenseCategory)
                .amount(new BigDecimal("1330.7"))
                .build();
        Operation operation2 = Operation.builder()
                .id(2L)
                .costCategory(expenseCategory)
                .amount(new BigDecimal(6000))
                .build();
        Operation operation3 = Operation.builder()
                .id(3L)
                .costCategory(expenseCategory)
                .amount(new BigDecimal("2861.52"))
                .build();
        Operation operation4 = Operation.builder()
                .id(4L)
                .costCategory(incomeCategory)
                .amount(new BigDecimal("9831.07"))
                .build();
        List<Operation> operations = List.of(operation1, operation2, operation3, operation4);
        BigDecimal expense = operationService.getSum(operations, CostCategory.CostCategoryType.EXPENSE);
        assertEquals(new BigDecimal("10192.22"), expense);
        BigDecimal income = operationService.getSum(operations, CostCategory.CostCategoryType.INCOME);
        assertEquals(new BigDecimal("9831.07"), income);
    }
}