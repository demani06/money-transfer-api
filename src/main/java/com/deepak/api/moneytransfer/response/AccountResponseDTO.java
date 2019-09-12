package com.deepak.api.moneytransfer.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDTO {

    private String accountNumber;
    private String sortCode;
    private String balance;

}
