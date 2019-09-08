package com.deepak.api.moneytransfer.model;

public class InvalidAccountException extends Throwable {
    private String errorMessage;

    public InvalidAccountException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public InvalidAccountException(String message, String errorMessage) {
        super(message);
        this.errorMessage = errorMessage;
    }

    public InvalidAccountException(String message, Throwable cause, String errorMessage) {
        super(message, cause);
        this.errorMessage = errorMessage;
    }

    public InvalidAccountException(Throwable cause, String errorMessage) {
        super(cause);
        this.errorMessage = errorMessage;
    }

    public InvalidAccountException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String errorMessage) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorMessage = errorMessage;
    }
}
