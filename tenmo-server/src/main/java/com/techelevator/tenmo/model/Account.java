package com.techelevator.tenmo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

public class Account {
    private int account_id;
    private String account_name;
    private int user_id;
    private BigDecimal balance;

    public Account(){};

    public Account(int account_id, String account_name, int user_id, BigDecimal balance){
        this.account_id = account_id;
        this.account_name = account_name;
        this.user_id = user_id;
        this.balance = balance;
    }

    public int getAccount_id() {
        return account_id;
    }

    public void setAccount_id(int account_id) {
        this.account_id = account_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getAccount_name() {
        return account_name;
    }

    public void setAccount_name(String account_name) {
        this.account_name = account_name;
    }
}
