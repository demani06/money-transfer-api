package com.deepak.api.moneytransfer.exception;

/*
* Custom exception to
* */

import lombok.AllArgsConstructor;

public class InsufficientFundsException extends Exception {

    private String errorMessage;

    public InsufficientFundsException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public InsufficientFundsException(String message, String errorMessage) {
        super(message);
        this.errorMessage = errorMessage;
    }

    public InsufficientFundsException(String message, Throwable cause, String errorMessage) {
        super(message, cause);
        this.errorMessage = errorMessage;
    }

    public InsufficientFundsException(Throwable cause, String errorMessage) {
        super(cause);
        this.errorMessage = errorMessage;
    }

    public InsufficientFundsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String errorMessage) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorMessage = errorMessage;
    }
}
