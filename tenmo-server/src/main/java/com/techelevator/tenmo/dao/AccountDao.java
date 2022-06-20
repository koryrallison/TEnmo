package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountDao {
    Account findById(long accountId);

    List<Account> findAll();

    Account findByUserId(long userId);

    boolean create(long accountId, long userId, BigDecimal balance);

    boolean update(Account account);
}
