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

    public Account(Long accountNumber, Long sortCode) {
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
    }
}
