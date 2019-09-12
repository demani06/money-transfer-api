package com.deepak.api.moneytransfer.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    private Long accountNumber;
    private Long sortCode;
    private BigDecimal balance;

    //A customer could have multiple accounts
    //Also customer cannot exist without an account
    private Customer customer;

    public Account(Long accountNumber, Long sortCode) {
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
        this.balance = new BigDecimal(0); //min value is 0
    }
}
