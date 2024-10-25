package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.entities.CostCategory;
import kz.romanb.onelabproject.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CostCategoryRepository extends JpaRepository<CostCategory, Long> {
    List<CostCategory> findAllByUser(User user);
}
