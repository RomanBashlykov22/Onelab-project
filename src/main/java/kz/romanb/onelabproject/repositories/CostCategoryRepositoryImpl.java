package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.entities.CostCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CostCategoryRepositoryImpl implements CostCategoryRepository {
    private final JdbcTemplate jdbcTemplate;
    private static RowMapper<CostCategory> rowMapper = (r, i) -> CostCategory.builder()
            .id(r.getLong("id"))
            .name(r.getString("name"))
            .categoryType(CostCategory.CostCategoryType.valueOf(r.getString("category_type")))
            .build();
    @Override
    public List<CostCategory> findAllUserCostCategories(Long userId) {
        String sql = "SELECT * FROM cost_category WHERE user_id=?";
        List<CostCategory> costCategories = jdbcTemplate.query(sql, rowMapper, userId);
        costCategories.forEach(c -> c.setUserId(userId));
        return costCategories;
    }

    @Override
    public CostCategory save(CostCategory costCategory) {
        String sql = "INSERT INTO cost_category (user_id, name, category_type) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setLong(1, costCategory.getUserId());
            preparedStatement.setString(2, costCategory.getName());
            preparedStatement.setString(3, costCategory.getCategoryType().name());
            return preparedStatement;
        }, keyHolder);
        costCategory.setId(keyHolder.getKey().longValue());
        return costCategory;
    }

    @Override
    public Optional<CostCategory> findById(Long id) {
        String sql = "SELECT * FROM cost_category WHERE id=?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
    }

    @Override
    public List<CostCategory> findAll() {
        String sql = "SELECT * FROM cost_category";
        return jdbcTemplate.query(sql, rowMapper);
    }
}
