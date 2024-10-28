package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.entities.BankAccount;
import kz.romanb.onelabproject.entities.CostCategory;
import kz.romanb.onelabproject.entities.User;
import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.kafka.KafkaService;
import kz.romanb.onelabproject.repositories.CostCategoryRepository;
import org.junit.jupiter.api.Assertions;
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

class CostCategoryServiceTest {
    @Mock
    CostCategoryRepository costCategoryRepository;
    @Mock
    KafkaService kafkaService;
    @InjectMocks
    CostCategoryService costCategoryService;

    User user = null;
    CostCategory costCategory1 = null;
    CostCategory costCategory2 = null;
    CostCategory costCategory3 = null;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder()
                .id(1L)
                .name("Username")
                .build();
        costCategory1 = CostCategory.builder()
                .id(1L)
                .name("Sport")
                .categoryType(CostCategory.CostCategoryType.EXPENSE)
                .build();
        costCategory2 = CostCategory.builder()
                .id(2L)
                .name("Salary")
                .categoryType(CostCategory.CostCategoryType.INCOME)
                .build();
        costCategory3 = CostCategory.builder()
                .id(3L)
                .name("Shopping")
                .categoryType(CostCategory.CostCategoryType.EXPENSE)
                .build();
        user.getCostCategories().add(costCategory1);
        user.getCostCategories().add(costCategory2);
        user.getCostCategories().add(costCategory3);
        costCategory1.setUser(user);
        costCategory2.setUser(user);
        costCategory3.setUser(user);
    }

    @Test
    void testGetAllUserCostCategories() {
        when(costCategoryRepository.findAllByUser(user)).thenReturn(user.getCostCategories());
        List<CostCategory> costCategories = costCategoryService.getAllUserCostCategories(user);
        assertNotNull(costCategories);
        assertEquals(costCategories.size(), user.getCostCategories().size());
        verify(costCategoryRepository, times(1)).findAllByUser(user);
    }

    @Test
    void testAddNewCostCategoryToUserWhenCostCategoryWithNameAlreadyExists() {
        CostCategory costCategoryWithNameAlreadyExists = CostCategory.builder().name("Sport").categoryType(CostCategory.CostCategoryType.EXPENSE).build();
        assertThrows(IllegalArgumentException.class, () -> costCategoryService.addNewCostCategoryToUser(user, costCategoryWithNameAlreadyExists));
        verify(costCategoryRepository, never()).save(any());
    }

    @Test
    void testAddNewCostCategoryToUserWhenCostCategoryTypeIsNull() {
        CostCategory costCategoryWithNameAlreadyExists = CostCategory.builder().name("New Category").build();
        assertThrows(IllegalArgumentException.class, () -> costCategoryService.addNewCostCategoryToUser(user, costCategoryWithNameAlreadyExists));
        verify(costCategoryRepository, never()).save(any());
    }

    @Test
    void testAddNewCostCategoryToUser() {
        CostCategory costCategory = CostCategory.builder().name("New Category").categoryType(CostCategory.CostCategoryType.INCOME).build();
        when(costCategoryRepository.save(costCategory)).thenReturn(CostCategory.builder().id(4L).name("New Category").categoryType(CostCategory.CostCategoryType.INCOME).user(user).build());
        CostCategory result = costCategoryService.addNewCostCategoryToUser(user, costCategory);
        assertNotNull(result);
        assertTrue(user.getCostCategories().stream().anyMatch(c -> c.getId().equals(4L)));
        assertEquals(4L, result.getId());
        assertEquals(costCategory.getName(), result.getName());
        verify(costCategoryRepository, times(1)).save(costCategory);
    }

    @Test
    void testFindByIdWhenCostCategoryExists() {
        Long costCategoryId = 1L;
        when(costCategoryRepository.findById(costCategoryId)).thenReturn(Optional.of(costCategory1));
        Optional<CostCategory> result = costCategoryService.findById(costCategoryId);
        assertTrue(result.isPresent());
        assertEquals(costCategoryId, result.get().getId());
        assertEquals(result.get().getName(), costCategory1.getName());
        assertEquals(result.get().getCategoryType(), costCategory1.getCategoryType());
        verify(costCategoryRepository, times(1)).findById(costCategoryId);
    }

    @Test
    void testFindByIdWhenCostCategoryDoesNotExists() {
        Long costCategoryId = 1L;
        when(costCategoryRepository.findById(costCategoryId)).thenReturn(Optional.empty());
        assertThrows(DBRecordNotFoundException.class, () -> costCategoryService.findById(costCategoryId));
        verify(costCategoryRepository, times(1)).findById(costCategoryId);
    }
}