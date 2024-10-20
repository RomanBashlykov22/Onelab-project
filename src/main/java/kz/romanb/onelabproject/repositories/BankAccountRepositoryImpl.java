package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.entities.BankAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BankAccountRepositoryImpl implements BankAccountRepository {
    private final JdbcTemplate jdbcTemplate;
    private static RowMapper<BankAccount> rowMapper = (r, i) -> BankAccount.builder()
            .id(r.getLong("id"))
            .name(r.getString("name"))
            .balance(r.getBigDecimal("balance"))
            .build();

    @Override
    public BankAccount save(BankAccount bankAccount) {
        String sql = "INSERT INTO bank_account (user_id, name, balance) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setLong(1, bankAccount.getUserId());
            preparedStatement.setString(2, bankAccount.getName());
            preparedStatement.setBigDecimal(3, bankAccount.getBalance());
            return preparedStatement;
        }, keyHolder);
        bankAccount.setId(keyHolder.getKey().longValue());
        return bankAccount;
    }

    @Override
    public Optional<BankAccount> findById(Long id) {
        String sql = "SELECT * FROM bank_account WHERE id=?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
    }

    @Override
    public List<BankAccount> findAll() {
        String sql = "SELECT * FROM bank_account";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<BankAccount> findAllUserBankAccounts(Long userId){
        String sql = "SELECT * FROM bank_account WHERE user_id=?";
        List<BankAccount> bankAccounts = jdbcTemplate.query(sql, rowMapper, userId);
        bankAccounts.forEach(b -> b.setUserId(userId));
        return bankAccounts;
    }

    @Override
    public void changeBalance(Long id, BigDecimal balance) {
        String sql = "UPDATE bank_account SET balance = ? WHERE id = ?";
        jdbcTemplate.update(sql, balance, id);
    }
}
