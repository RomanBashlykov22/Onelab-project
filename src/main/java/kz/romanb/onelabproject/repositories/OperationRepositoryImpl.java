package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.entities.BankAccount;
import kz.romanb.onelabproject.entities.CostCategory;
import kz.romanb.onelabproject.entities.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OperationRepositoryImpl implements OperationRepository {
    private final JdbcTemplate jdbcTemplate;
    private static RowMapper<Operation> rowMapper = (r, i) -> Operation.builder()
            .id(r.getLong("id"))
            .date(r.getDate("date").toLocalDate())
            .amount(r.getBigDecimal("amount"))
            .bankAccount(BankAccount.builder().id(r.getLong("bank_account_id")).build())
            .costCategory(CostCategory.builder().id(r.getLong("cost_category_id")).build())
            .build();

    @Override
    public List<Operation> findAllOperationsByBankAccount(BankAccount bankAccount) {
        String sql = "SELECT * FROM operation WHERE bank_account_id = ?";
        return jdbcTemplate.query(sql, rowMapper, bankAccount.getId());
    }

    @Override
    public List<Operation> findAllOperationsByCostCategory(CostCategory costCategory) {
        String sql = "SELECT * FROM operation WHERE cost_category_id = ?";
        return jdbcTemplate.query(sql, rowMapper, costCategory.getId());
    }

    @Override
    public List<Operation> findAllOperationsForDate(LocalDate date) {
        String sql = "SELECT * FROM operation WHERE date = ?";
        return jdbcTemplate.query(sql, rowMapper, date);
    }

    @Override
    public List<Operation> findAllOperationsBetweenDates(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM operation WHERE date BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, rowMapper, startDate, endDate);
    }

    @Override
    public Operation save(Operation operation) {
        String sql = "INSERT INTO operation(amount, date, bank_account_id, cost_category_id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setBigDecimal(1, operation.getAmount());
            preparedStatement.setDate(2, Date.valueOf(operation.getDate()));
            preparedStatement.setLong(3, operation.getBankAccount().getId());
            preparedStatement.setLong(4, operation.getCostCategory().getId());
            return preparedStatement;
        }, keyHolder);
        operation.setId(keyHolder.getKey().longValue());
        return operation;
    }

    @Override
    public Optional<Operation> findById(Long id) {
        String sql = "SELECT * FROM operation WHERE id=?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
    }

    @Override
    public List<Operation> findAll() {
        String sql = "SELECT * FROM operation";
        return jdbcTemplate.query(sql, rowMapper);
    }
}
