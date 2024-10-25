package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.entities.CostCategory;
import kz.romanb.onelabproject.entities.User;
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
    private final CostCategoryRepository costCategoryRepository;

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public List<CostCategory> getAllUserCostCategories(User user){
        return costCategoryRepository.findAllByUser(user);
    }

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
    public CostCategory addNewCostCategoryToUser(User user, CostCategory costCategory) {
        user.getCostCategories().stream()
                .filter(c -> c.getName().equals(costCategory.getName()))
                .findAny()
                .ifPresent(b -> {
                    throw new IllegalArgumentException("У пользователя уже есть такая категория расходов");
                });
        if(costCategory.getCategoryType() == null){
            throw new IllegalArgumentException("Не указан тип категории");
        }
        CostCategory saved = costCategoryRepository.save(costCategory);
        user.getCostCategories().add(saved);
        return saved;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public Optional<CostCategory> findById(Long id) {
        Optional<CostCategory> costCategoryOptional = costCategoryRepository.findById(id);
        if(costCategoryOptional.isEmpty()){
            throw new DBRecordNotFoundException("Категории с id " + id + " не существует");
        }
        return costCategoryOptional;
    }
}
