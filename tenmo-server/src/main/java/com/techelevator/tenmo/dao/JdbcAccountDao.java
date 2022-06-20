package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
public class JdbcAccountDao implements AccountDao{

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Account findById(long accountId) {
        String sql = "SELECT account_id, user_id, balance FROM account WHERE account_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, accountId);
        if (rowSet.next()){
            return mapRowToAccount(rowSet);
        }
        throw new RecoverableDataAccessException("Account " + accountId + " was not found.");
    }

    @Override
    public List<Account> findAll() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT account_id, user_id, balance FROM account;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while(results.next()) {
            Account account = mapRowToAccount(results);
            accounts.add(account);
        }
        return accounts;
    }

    @Override
    public Account findByUserId(long userId) {
        String sql = "SELECT account_id, user_id, balance FROM account WHERE user_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, userId);
        if (rowSet.next()){
            return mapRowToAccount(rowSet);
        }
        throw new RecoverableDataAccessException("Account of User " + userId + " was not found.");
    }

    @Override
    public boolean create(long accountId, long userId, BigDecimal balance) {
        String sql = "INSERT INTO account (account_id, user_id, balance) " +
                "VALUES (?, ?, ?) RETURNING account_id;";
        Integer newAccountId;
        try {
            newAccountId = jdbcTemplate.queryForObject(sql, Integer.class, accountId, userId, balance);
        } catch (DataAccessException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean update(Account account) {
        String sql = "UPDATE account SET account_id = ?, user_id = ?, balance = ?) " +
                "WHERE account_id = ?";
        try {
            jdbcTemplate.update(sql, account.getAccount_id(), account.getUserId(), account.getBalance());
        } catch (DataAccessException e) {
            return false;
        }
        return true;
    }

    private Account mapRowToAccount(SqlRowSet rs){
        Account account = new Account();
        account.setAccount_id(rs.getInt("account_id"));
        account.setUserId(rs.getInt("user_id"));
        account.setBalance(rs.getBigDecimal("balance"));
        return account;
    }
}
