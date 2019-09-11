package com.deepak.api.moneytransfer.service;

import com.deepak.api.moneytransfer.request.TransactionRequestDTO;
import io.vertx.ext.web.RoutingContext;

public interface MoneyTransferService {

    void setUpInitialData();

    void handleTransactionsByAccountId(RoutingContext routingContext);

    void handleGetAccounts(RoutingContext routingContext);

    void handleTransferMoney(RoutingContext routingContext);

    void debitSourceAccount(TransactionRequestDTO transactionRequestDTO);

    void creditDestinationAccount(TransactionRequestDTO transactionRequestDTO);
}
