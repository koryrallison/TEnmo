package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountDao {
    Account findById(long account_id);

    List<Account> findAll();

    List<Account> findByUserId(long user_id);

    int create(String account_name, long user_id, BigDecimal balance);

    boolean update(Account account);
}
