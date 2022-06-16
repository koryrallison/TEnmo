package com.techelevator.tenmo.model;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter

public class Account {

    private int accountId;
    private int userId;
    private BigDecimal balance;

}
