package com.deepak.api.moneytransfer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

    private UUID customerId;
    private String firstName;
    private String lastName;
    private String emailAddress;

}
