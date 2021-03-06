package com.deepak.api.moneytransfer.request;

import com.deepak.api.moneytransfer.model.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequestDTO {

    private Account sourceAccount;
    private Account destinationAccount;
    private BigDecimal transferAmount;


}
