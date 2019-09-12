package com.deepak.api.moneytransfer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

    private String customerId;
    private String firstName;
    private String lastName;
    private String emailAddress;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    //For some strange reason the application is not directly working with LocalDate which needs to be debugged
    private LocalDate dateOfBirth;


}
