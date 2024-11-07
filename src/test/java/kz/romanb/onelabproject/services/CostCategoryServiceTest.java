package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.models.dto.CostCategoryDto;
import kz.romanb.onelabproject.models.entities.CostCategory;
import kz.romanb.onelabproject.models.entities.User;
import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.repositories.CostCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CostCategoryServiceTest {
    @Mock
    CostCategoryRepository costCategoryRepository;
    @Mock
    UserService userService;
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
                .username("Username")
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
        when(userService.findUserById(1L)).thenReturn(Optional.of(user));
        when(costCategoryRepository.findAllByUserId(user.getId())).thenReturn(user.getCostCategories());

        List<CostCategory> costCategories = costCategoryService.getAllUserCostCategories(user.getId());

        assertNotNull(costCategories);
        assertEquals(costCategories.size(), user.getCostCategories().size());
        verify(costCategoryRepository, times(1)).findAllByUserId(user.getId());
    }

    @Test
    void testAddNewCostCategoryToUserWhenUserDoesNotExists() {
        when(userService.findUserById(user.getId())).thenReturn(Optional.empty());

        assertThrows(DBRecordNotFoundException.class, () -> costCategoryService.addNewCostCategoryToUser(user.getId(), CostCategoryDto.builder().build()));

        verify(costCategoryRepository, never()).findAllByUserId(user.getId());
        verify(costCategoryRepository, never()).save(any());
    }

    @Test
    void testAddNewCostCategoryToUserWhenCostCategoryWithNameAlreadyExists() {
        CostCategoryDto costCategoryWithNameAlreadyExists = CostCategoryDto.builder().name("Sport").categoryType(CostCategory.CostCategoryType.EXPENSE).build();
        when(userService.findUserById(user.getId())).thenReturn(Optional.of(user));
        when(costCategoryRepository.findAllByUserId(user.getId())).thenReturn(user.getCostCategories());

        assertThrows(IllegalArgumentException.class, () -> costCategoryService.addNewCostCategoryToUser(user.getId(), costCategoryWithNameAlreadyExists));

        verify(costCategoryRepository, never()).save(any());
    }

    @Test
    void testAddNewCostCategoryToUserWhenCostCategoryTypeIsNull() {
        CostCategoryDto costCategoryWithNameAlreadyExists = CostCategoryDto.builder().name("New Category").build();
        when(userService.findUserById(user.getId())).thenReturn(Optional.of(user));
        when(costCategoryRepository.findAllByUserId(user.getId())).thenReturn(user.getCostCategories());

        assertThrows(IllegalArgumentException.class, () -> costCategoryService.addNewCostCategoryToUser(user.getId(), costCategoryWithNameAlreadyExists));

        verify(costCategoryRepository, never()).save(any());
    }

    @Test
    void testAddNewCostCategoryToUser() {
        CostCategoryDto costCategory = CostCategoryDto.builder().name("New Category").categoryType(CostCategory.CostCategoryType.INCOME).build();
        when(userService.findUserById(user.getId())).thenReturn(Optional.of(user));
        when(costCategoryRepository.findAllByUserId(user.getId())).thenReturn(user.getCostCategories());
        when(costCategoryRepository.save(any(CostCategory.class))).thenReturn(CostCategory.builder().id(4L).name("New Category").categoryType(CostCategory.CostCategoryType.INCOME).user(user).build());

        CostCategory result = costCategoryService.addNewCostCategoryToUser(user.getId(), costCategory);

        assertNotNull(result);
        assertEquals(4L, result.getId());
        assertEquals(costCategory.getName(), result.getName());
        verify(costCategoryRepository, times(1)).save(any(CostCategory.class));
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
        when(costCategoryRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<CostCategory> result = costCategoryService.findById(1L);

        assertTrue(result.isEmpty());
        verify(costCategoryRepository, times(1)).findById(1L);
    }
}