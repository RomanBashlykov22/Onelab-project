package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.entities.BankAccount;
import kz.romanb.onelabproject.entities.CostCategory;
import kz.romanb.onelabproject.entities.Operation;
import kz.romanb.onelabproject.entities.User;
import kz.romanb.onelabproject.repositories.OperationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

class OperationServiceTest {
    @Mock
    OperationRepository operationRepository;
    @Mock
    BankAccountService bankAccountService;
    @InjectMocks
    OperationService operationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOperation() {

    }

    @Test
    void testFindOperationById() {

    }

    @Test
    void testFindAllOperations() {

    }

    @Test
    void testFindAllOperationsByUser() {

    }

    @Test
    void testFindAllOperationsByCostCategory() {

    }

    @Test
    void testFindAllOperationsForDate() {

    }

    @Test
    void testFindAllOperationsBetweenDates() {

    }

    @Test
    void testGetSum() {

    }
}