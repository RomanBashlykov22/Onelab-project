package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.entities.CostCategory;
import kz.romanb.onelabproject.entities.User;
import kz.romanb.onelabproject.repositories.CostCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CostCategoryService {
    private final CostCategoryRepository costCategoryRepository;

    public List<CostCategory> getAllUserCostCategories(User user){
        List<CostCategory> allUserCostCategories = costCategoryRepository.findAllUserCostCategories(user.getId());
        user.setCostCategories(allUserCostCategories);
        return allUserCostCategories;
    }

    public CostCategory addNewCostCategoryToUser(User user, CostCategory costCategory) {
        user.getCostCategories().stream()
                .filter(c -> c.getName().equals(costCategory.getName()))
                .findAny()
                .ifPresent(b -> {
                    throw new IllegalArgumentException("У пользователя уже есть такая категория расходов");
                });
        user.getCostCategories().add(costCategory);
        costCategoryRepository.save(costCategory);
        return costCategory;
    }

    public CostCategory findById(Long id) {
        return costCategoryRepository.findById(id).get();
    }
}
