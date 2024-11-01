package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.models.entities.CostCategory;
import kz.romanb.onelabproject.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CostCategoryRepository extends JpaRepository<CostCategory, Long> {
    List<CostCategory> findAllByUserId(Long userId);
}
