package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.entities.CostCategory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CostCategoryRepositoryImpl implements CostCategoryRepository {
    public static final List<CostCategory> costCategories = new ArrayList<>();

    @Override
    public CostCategory save(CostCategory costCategory) {
        Optional<CostCategory> costCategoryOptional = findById(costCategory.getId());
        costCategoryOptional.ifPresent(costCategories::remove);
        costCategories.add(costCategory);
        ;
        return costCategory;
    }

    @Override
    public Optional<CostCategory> findById(Long id) {
        return costCategories.stream().filter(c -> c.getId() == id).findFirst();
    }

    @Override
    public List<CostCategory> findAll() {
        return costCategories;
    }
}
