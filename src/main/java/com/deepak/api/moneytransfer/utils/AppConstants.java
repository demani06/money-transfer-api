package com.deepak.api.moneytransfer.utils;

/*
 * This class holds the constants required for the API
 * */

public class AppConstants {
    public static final Integer SERVER_PORT = 6090;

    public static final String API_FOR_TRANSACTIONS = "/api/transactions";
    public static final String API_FOR_GET_ACCOUNT_TRANSACTIONS = "/api/accounts/:id/transactions/";
    public static final String API_FOR_ACCOUNTS_FOR_A_CUSTOMER = "/api/customer/:id/accounts";
    public static final String API_FOR_ACCOUNTS = "/api/accounts";

}
