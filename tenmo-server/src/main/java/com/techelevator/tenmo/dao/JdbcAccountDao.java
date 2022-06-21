package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    public Account findById(long account_id) {
        String sql = "SELECT account_id, account_name, user_id, balance FROM account WHERE account_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, account_id);
        if (rowSet.next()){
            return mapRowToAccount(rowSet);
        }
        throw new RecoverableDataAccessException("Account " + account_id + " was not found.");
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
    public List<Account> findByUserId(long user_id) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT account_id, account_name, user_id, balance FROM account WHERE user_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, user_id);
        while(results.next()) {
            Account account = mapRowToAccount(results);
            accounts.add(account);
        }
        return accounts;
    }

    @Override
    public int create(String account_name, long user_id, BigDecimal balance) {
        String sql = "INSERT INTO account (account_name, user_id, balance) " +
                "VALUES (?, ?, ?) RETURNING account_id;";
        Integer newAccountId;
        try {
            newAccountId = jdbcTemplate.queryForObject(sql, Integer.class, account_name, user_id, balance);
        } catch (DataAccessException e) {
            return -1;
        }
        return newAccountId;
    }

    @Override
    public boolean update(Account account) {
        String sql = "UPDATE account SET account_name = ?, user_id = ?, balance = ?) " +
                "WHERE account_id = ?";
        try {
            jdbcTemplate.update(sql, account.getAccount_name(), account.getUser_id(), account.getBalance(), account.getAccount_id());
        } catch (DataAccessException e) {
            return false;
        }
        return true;
    }

    private Account mapRowToAccount(SqlRowSet rs){
        Account account = new Account();
        account.setAccount_id(rs.getInt("account_id"));
        account.setAccount_name(rs.getString("account_name"));
        account.setUser_id(rs.getInt("user_id"));
        account.setBalance(rs.getBigDecimal("balance"));
        return account;
    }
}
