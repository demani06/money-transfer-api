package com.deepak.api.moneytransfer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

/*
* This class is for creating a log for each transaction and it holds the accounts used and amount
* */

@Data
public class MoneyTransaction {

    private UUID transactionId;
    private Long sourceAccountNumber;
    private Long destinationAccountNumber;
    private BigDecimal transactionAmount;

    public MoneyTransaction( Long sourceAccountNumber, Long destinationAccountNumber, BigDecimal transactionAmount) {
        this.transactionId = UUID.randomUUID();
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.transactionAmount = transactionAmount;
    }

    //Audit details like created by, modified by, created on and modified date can be added later

}
