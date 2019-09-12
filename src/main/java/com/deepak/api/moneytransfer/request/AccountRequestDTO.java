package com.deepak.api.moneytransfer.request;

import com.deepak.api.moneytransfer.model.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountRequestDTO {

    private Long accountNumber;
    private Long sortCode;
    private Customer customer;
}
