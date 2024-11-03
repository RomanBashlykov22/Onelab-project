package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.models.dto.CostCategoryDto;
import kz.romanb.onelabproject.models.dto.OperationDto;
import kz.romanb.onelabproject.models.dto.SumResponse;
import kz.romanb.onelabproject.models.entities.BankAccount;
import kz.romanb.onelabproject.models.entities.CostCategory;
import kz.romanb.onelabproject.models.entities.Operation;
import kz.romanb.onelabproject.models.entities.User;
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
    @Mock
    CostCategoryService costCategoryService;
    @Mock
    UserService userService;
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
                .username("Username")
                .bankAccounts(List.of(bankAccountWithBalance, bankAccountWithoutBalance))
                .costCategories(List.of(expenseCategory, incomeCategory))
                .build();
    }

    @Test
    void testCreateOperationWhenBankAccountDoesNotExists() {
        when(bankAccountService.findById(any())).thenReturn(Optional.empty());

        assertThrows(DBRecordNotFoundException.class, () -> operationService.createOperation(1L, 1L, new BigDecimal(10)));

        verify(bankAccountService, times(1)).findById(any());
        verify(costCategoryService, never()).findById(any());
        verify(bankAccountService, never()).changeBalance(any(), any());
        verify(operationRepository, never()).save(any());
    }

    @Test
    void testCreateOperationWhenCostCategoryDoesNotExists() {
        when(bankAccountService.findById(any())).thenReturn(Optional.of(bankAccountWithBalance));
        when(costCategoryService.findById(any())).thenReturn(Optional.empty());

        assertThrows(DBRecordNotFoundException.class, () -> operationService.createOperation(1L, 1L, new BigDecimal(10)));

        verify(bankAccountService, times(1)).findById(any());
        verify(costCategoryService, times(1)).findById(any());
        verify(bankAccountService, never()).changeBalance(any(), any());
        verify(operationRepository, never()).save(any());
    }

    @Test
    void testCreateIncomeOperation() {
        BigDecimal amount = new BigDecimal(5000);
        BigDecimal expectedBalance = bankAccountWithBalance.getBalance().add(amount);
        Operation op = Operation.builder()
                .id(10L)
                .date(LocalDate.now())
                .bankAccount(bankAccountWithBalance)
                .costCategory(incomeCategory)
                .amount(amount).build();
        when(bankAccountService.findById(bankAccountWithBalance.getId())).thenReturn(Optional.of(bankAccountWithBalance));
        when(costCategoryService.findById(incomeCategory.getId())).thenReturn(Optional.of(incomeCategory));
        when(bankAccountService.changeBalance(any(), any())).thenReturn("Баланс изменен");
        when(operationRepository.save(any(Operation.class))).thenReturn(op);

        operationService.createOperation(bankAccountWithBalance.getId(), incomeCategory.getId(), amount);

        verify(bankAccountService, times(1)).findById(bankAccountWithBalance.getId());
        verify(costCategoryService, times(1)).findById(incomeCategory.getId());
        verify(bankAccountService, times(1)).changeBalance(bankAccountWithBalance.getId(), expectedBalance);
        verify(operationRepository).save(argThat(operation ->
                operation.getBankAccount().equals(bankAccountWithBalance) &&
                        operation.getCostCategory().equals(incomeCategory) &&
                        operation.getAmount().equals(amount) &&
                        operation.getDate().equals(LocalDate.now())
        ));
    }

    @Test
    void testCreateExpenseOperationWhenNotEnoughMoney() {
        when(bankAccountService.findById(any())).thenReturn(Optional.of(bankAccountWithBalance));
        when(costCategoryService.findById(any())).thenReturn(Optional.of(expenseCategory));
        BigDecimal amount = new BigDecimal(5000);

        assertThrows(NotEnoughMoneyException.class, () -> operationService.createOperation(bankAccountWithBalance.getId(), expenseCategory.getId(), amount));

        verify(bankAccountService, times(1)).findById(any());
        verify(costCategoryService, times(1)).findById(any());
        verify(bankAccountService, never()).changeBalance(any(), any());
        verify(operationRepository, never()).save(any());
    }

    @Test
    void testCreateExpenseOperation() {
        BigDecimal amount = new BigDecimal(500);
        BigDecimal expectedBalance = bankAccountWithBalance.getBalance().subtract(amount);
        Operation op = Operation.builder()
                .id(10L)
                .date(LocalDate.now())
                .bankAccount(bankAccountWithBalance)
                .costCategory(expenseCategory)
                .amount(amount).build();
        when(bankAccountService.findById(bankAccountWithBalance.getId())).thenReturn(Optional.of(bankAccountWithBalance));
        when(costCategoryService.findById(expenseCategory.getId())).thenReturn(Optional.of(expenseCategory));
        when(bankAccountService.changeBalance(any(), any())).thenReturn("Баланс изменен");
        when(operationRepository.save(any(Operation.class))).thenReturn(op);

        operationService.createOperation(bankAccountWithBalance.getId(), expenseCategory.getId(), amount);

        verify(bankAccountService, times(1)).findById(bankAccountWithBalance.getId());
        verify(costCategoryService, times(1)).findById(expenseCategory.getId());
        verify(bankAccountService, times(1)).changeBalance(bankAccountWithBalance.getId(), expectedBalance);
        verify(operationRepository).save(argThat(operation ->
                operation.getBankAccount().equals(bankAccountWithBalance) &&
                        operation.getCostCategory().equals(expenseCategory) &&
                        operation.getAmount().equals(amount) &&
                        operation.getDate().equals(LocalDate.now())
        ));
    }

    @Test
    void testFindOperationByIdWhenExists() {
        when(operationRepository.findById(1L)).thenReturn(Optional.of(Operation.builder().id(1L).build()));

        Optional<Operation> result = operationService.findOperationById(1L);

        assertTrue(result.isPresent());
        verify(operationRepository, times(1)).findById(1L);
    }

    @Test
    void testFindOperationByIdWhenDoesNotExists() {
        when(operationRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Operation> result = operationService.findOperationById(1L);

        assertTrue(result.isEmpty());
        verify(operationRepository, times(1)).findById(1L);
    }

    @Test
    void testFindAllOperationsByUserWhenUserDoesNotExists() {
        when(userService.findUserById(any())).thenReturn(Optional.empty());

        assertThrows(DBRecordNotFoundException.class, () -> operationService.findAllOperationsByUser(1L));

        verify(operationRepository, never()).findAllByBankAccount(any());
    }

    @Test
    void testFindAllOperationsByUser() {
        Operation operation1 = Operation.builder().date(LocalDate.of(2024, 10, 1)).build();
        Operation operation2 = Operation.builder().date(LocalDate.of(2024, 9, 1)).build();
        Operation operation3 = Operation.builder().date(LocalDate.of(2024, 8, 1)).build();
        when(userService.findUserById(user.getId())).thenReturn(Optional.of(user));
        when(operationRepository.findAllByBankAccount(bankAccountWithBalance)).thenReturn(List.of(operation1, operation2));
        when(operationRepository.findAllByBankAccount(bankAccountWithoutBalance)).thenReturn(List.of(operation3));

        List<Operation> operations = operationService.findAllOperationsByUser(user.getId());

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
        when(userService.findUserById(user.getId())).thenReturn(Optional.of(user));
        when(operationRepository.findAllByBankAccount(any())).thenReturn(new ArrayList<>());

        List<Operation> operations = operationService.findAllOperationsByUser(user.getId());

        assertTrue(operations.isEmpty());
        verify(operationRepository, never()).findAllByBankAccount(any());
    }

    @Test
    void testFindAllOperationsByUserWithoutOperations() {
        when(userService.findUserById(user.getId())).thenReturn(Optional.of(user));
        when(operationRepository.findAllByBankAccount(bankAccountWithBalance)).thenReturn(new ArrayList<>());
        when(operationRepository.findAllByBankAccount(bankAccountWithoutBalance)).thenReturn(new ArrayList<>());

        List<Operation> operations = operationService.findAllOperationsByUser(user.getId());

        assertTrue(operations.isEmpty());
        verify(operationRepository, times(user.getBankAccounts().size())).findAllByBankAccount(any());
    }

    @Test
    void testFindAllOperationsByCostCategoryWhenCategoryDoesNotExists() {
        when(costCategoryService.findById(any())).thenReturn(Optional.empty());

        assertThrows(DBRecordNotFoundException.class, () -> operationService.findAllOperationsByCostCategory(expenseCategory.getId()));

        verify(operationRepository, never()).findAllByCostCategory(any());
    }

    @Test
    void testFindAllOperationsByCostCategory() {
        Operation operation1 = Operation.builder().costCategory(expenseCategory).date(LocalDate.of(2024, 10, 1)).build();
        Operation operation2 = Operation.builder().costCategory(expenseCategory).date(LocalDate.of(2024, 9, 1)).build();
        Operation operation3 = Operation.builder().costCategory(incomeCategory).date(LocalDate.of(2024, 8, 1)).build();
        when(costCategoryService.findById(expenseCategory.getId())).thenReturn(Optional.of(expenseCategory));
        when(costCategoryService.findById(incomeCategory.getId())).thenReturn(Optional.of(incomeCategory));
        when(operationRepository.findAllByCostCategory(expenseCategory)).thenReturn(List.of(operation1, operation2));
        when(operationRepository.findAllByCostCategory(incomeCategory)).thenReturn(List.of(operation3));

        List<Operation> expenseOperations = operationService.findAllOperationsByCostCategory(expenseCategory.getId());

        assertNotNull(expenseOperations);
        assertEquals(2, expenseOperations.size());
        assertEquals(operation1, expenseOperations.get(0));
        assertEquals(operation2, expenseOperations.get(1));
        verify(operationRepository, times(1)).findAllByCostCategory(expenseCategory);

        List<Operation> incomeOperations = operationService.findAllOperationsByCostCategory(incomeCategory.getId());

        assertNotNull(incomeOperations);
        assertEquals(1, incomeOperations.size());
        assertEquals(operation3, incomeOperations.get(0));
        verify(operationRepository, times(1)).findAllByCostCategory(incomeCategory);
    }

    @Test
    void testFindAllOperationsByCostCategoryWithoutOperations() {
        when(costCategoryService.findById(expenseCategory.getId())).thenReturn(Optional.of(expenseCategory));
        when(operationRepository.findAllByCostCategory(expenseCategory)).thenReturn(new ArrayList<>());

        List<Operation> operations = operationService.findAllOperationsByCostCategory(expenseCategory.getId());

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
        BigDecimal ex1 = new BigDecimal("1057.12");
        BigDecimal ex2 = new BigDecimal("720.45");
        BigDecimal ex = ex1.add(ex2);
        BigDecimal in1 = new BigDecimal(1200);
        BigDecimal in2 = new BigDecimal(70);
        BigDecimal in = in1.add(in2);
        OperationDto dto1 = OperationDto.builder().amount(ex1).costCategoryDto(CostCategoryDto.builder().categoryType(CostCategory.CostCategoryType.EXPENSE).build()).build();
        OperationDto dto2 = OperationDto.builder().amount(ex2).costCategoryDto(CostCategoryDto.builder().categoryType(CostCategory.CostCategoryType.EXPENSE).build()).build();
        OperationDto dto3 = OperationDto.builder().amount(in1).costCategoryDto(CostCategoryDto.builder().categoryType(CostCategory.CostCategoryType.INCOME).build()).build();
        OperationDto dto4 = OperationDto.builder().amount(in2).costCategoryDto(CostCategoryDto.builder().categoryType(CostCategory.CostCategoryType.INCOME).build()).build();
        List<OperationDto> operations = List.of(dto1, dto2, dto3, dto4);

        SumResponse sum = operationService.getSum(operations);

        assertEquals(operations.size(), sum.getAmountOfOperations());
        assertEquals(ex.doubleValue(), sum.getExpense().doubleValue());
        assertEquals(in.doubleValue(), sum.getIncome().doubleValue());
    }

    @Test
    void testFindAllOperations() {
        Operation operation1 = Operation.builder().build();
        Operation operation2 = Operation.builder().build();
        Operation operation3 = Operation.builder().date(LocalDate.of(2024, 8, 1)).build();
        when(operationRepository.findAll()).thenReturn(List.of(operation1, operation2, operation3));

        List<Operation> operations = operationService.findAllOperations();

        assertEquals(3, operations.size());
        verify(operationRepository, times(1)).findAll();
    }
}