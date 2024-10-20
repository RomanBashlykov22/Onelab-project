package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.entities.CostCategory;

import java.util.List;

public interface CostCategoryRepository extends Repository<CostCategory, Long> {
    List<CostCategory> findAllUserCostCategories(Long userId);
}
