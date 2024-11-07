package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.models.dto.CostCategoryDto;
import kz.romanb.onelabproject.models.entities.CostCategory;
import kz.romanb.onelabproject.models.entities.User;
import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.repositories.CostCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CostCategoryService {
    public static final String COST_CATEGORY_WITH_ID_DOES_NOT_EXISTS = "Категории с id %d не существует";
    private final CostCategoryRepository costCategoryRepository;
    private final UserService userService;

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public List<CostCategory> getAllUserCostCategories(Long userId) {
        if (userService.findUserById(userId).isEmpty()) {
            throw new DBRecordNotFoundException(String.format(UserService.USER_WITH_ID_DOES_NOT_EXISTS, userId));
        }
        return costCategoryRepository.findAllByUserId(userId);
    }

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
    public CostCategory addNewCostCategoryToUser(Long userId, CostCategoryDto costCategoryDto) {
        Optional<User> userOptional = userService.findUserById(userId);
        if (userOptional.isEmpty()) {
            throw new DBRecordNotFoundException(String.format(UserService.USER_WITH_ID_DOES_NOT_EXISTS, userId));
        }
        costCategoryRepository.findAllByUserId(userId).stream()
                .filter(c -> c.getName().equals(costCategoryDto.getName()))
                .findAny()
                .ifPresent(b -> {
                    throw new IllegalArgumentException("У пользователя уже есть такая категория расходов");
                });
        if (Optional.ofNullable(costCategoryDto.getCategoryType()).isEmpty()) {
            throw new IllegalArgumentException("Не указан тип категории");
        }
        CostCategory costCategory = CostCategory.builder()
                .user(userOptional.get())
                .name(costCategoryDto.getName())
                .categoryType(costCategoryDto.getCategoryType())
                .build();
        return costCategoryRepository.save(costCategory);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public Optional<CostCategory> findById(Long id) {
        return costCategoryRepository.findById(id);
    }
}
