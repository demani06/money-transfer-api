package com.deepak.api.moneytransfer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;


@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMessageResponse {

    private int statusCode;
    private String exceptionMessage;
    private LocalDateTime errorMessageTimeStamp;

}
