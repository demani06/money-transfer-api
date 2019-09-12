package com.deepak.api.moneytransfer.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
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

    public Account(Long sortCode, Customer customer) {
        //this.accountNumber = accountNumber;
        this.sortCode = sortCode;
        this.customer = customer;
        this.balance = new BigDecimal(0); //min value is 0
    }

    public Account(Long accountNumber, Long sortCode, BigDecimal balance, Customer customer) {
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
        this.balance = balance;
        this.customer = customer;
    }
}
