package com.deepak.api.moneytransfer;


import com.deepak.api.moneytransfer.model.MoneyTransaction;
import com.deepak.api.moneytransfer.model.TransactionRequestDTO;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class MoneyTransferVerticle extends AbstractVerticle {

    private Map<UUID, MoneyTransaction> transactionsMap = new HashMap<>();


    @Override
    public void start() {

        //TODO for api
        setUpInitialData();

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.post("/api/transactions").handler(this::handleTransferMoney);
        router.get("/api/transactions").handler(this::handleGetTransactions);

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    private void setUpInitialData() {

        //Basic test data for API
        MoneyTransaction sampleTransaction = new MoneyTransaction(90909090L, 99999999L, new BigDecimal(43000));
        transactionsMap.put(sampleTransaction.getTransactionId(), sampleTransaction);
    }

    private void handleGetTransactions(RoutingContext routingContext) {
        log.info("Start handleGetTransactions");

        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .setStatusCode(200)
                .end(Json.encodePrettily(transactionsMap.values()));
    }

    private void handleTransferMoney(RoutingContext routingContext) {

        log.info("Start handleTransferMoney");

        try {
            final TransactionRequestDTO transactionRequestDTO = Json.decodeValue(routingContext.getBodyAsString(), TransactionRequestDTO.class);

            log.debug("transactionRequestDTO=",  transactionRequestDTO);

            //TODO validation check on debit source account if amount be to be debited > balance amount of debit account
            //TODO validation check for null
            debitSourceAccount(transactionRequestDTO);
            creditDestinationAccount(transactionRequestDTO);

            //TODO refresh account balances in store

            MoneyTransaction moneyTransaction = new MoneyTransaction(transactionRequestDTO.getSourceAccount().getAccountNumber(),
                    transactionRequestDTO.getDestinationAccount().getAccountNumber(), transactionRequestDTO.getTransferAmount());

            log.info("values after funds transfer =", transactionRequestDTO);
            log.info("moneyTransaction values after funds transfer =", moneyTransaction.getTransactionId());

            transactionsMap.put(moneyTransaction.getTransactionId(), moneyTransaction);

            //return moneytransaction object in case of 201 (successful creation)
            routingContext.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(moneyTransaction));
        } catch (Exception e) {
            routingContext.response().setStatusCode(400).end();
        }
    }

    private void debitSourceAccount(TransactionRequestDTO transactionRequestDTO) {
        BigDecimal newBalance = transactionRequestDTO.getSourceAccount().getBalance().subtract(transactionRequestDTO.getTransferAmount());

        //update account balance
        transactionRequestDTO.getSourceAccount().setBalance(newBalance);
    }

    private void creditDestinationAccount(TransactionRequestDTO transactionRequestDTO) {
        BigDecimal newBalance = transactionRequestDTO.getDestinationAccount().getBalance().add(transactionRequestDTO.getTransferAmount());
        //update account balance
        transactionRequestDTO.getDestinationAccount().setBalance(newBalance);
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        //TODO wrap this request?
        response.setStatusCode(statusCode).end();
    }

}
