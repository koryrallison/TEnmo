package com.techelevator.tenmo.model;

public class RegistrationCredentials extends UserCredentials{

    private String account_name;

    public RegistrationCredentials(String username, String password, String account_name) {
        super(username, password);
        this.account_name = account_name;
    }

    public String getAccount_name() {
        return account_name;
    }

    public void setAccount_name(String account_name) {
        this.account_name = account_name;
    }
}
