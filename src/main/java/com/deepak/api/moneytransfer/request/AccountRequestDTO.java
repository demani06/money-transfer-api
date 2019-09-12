package com.deepak.api.moneytransfer.request;

import com.deepak.api.moneytransfer.model.Customer;
import com.deepak.api.moneytransfer.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountRequestDTO {

    private Long sortCode; //this is needed because the branch need to be sent
    private AccountType accountType;
    private Customer customer;
}
