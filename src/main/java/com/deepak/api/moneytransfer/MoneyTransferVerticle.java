package com.deepak.api.moneytransfer;


import com.deepak.api.moneytransfer.exception.InsufficientFundsException;
import com.deepak.api.moneytransfer.model.ErrorMessageResponse;
import com.deepak.api.moneytransfer.model.MoneyTransaction;
import com.deepak.api.moneytransfer.model.TransactionRequestDTO;
import com.deepak.api.moneytransfer.utils.AccountValidator;
import com.deepak.api.moneytransfer.utils.AppConstants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/*
 * This is the main class which boots the Vertx server and exposes the endpoints
 * */
@Slf4j
public class MoneyTransferVerticle extends AbstractVerticle {

    //TODO move this map to a repository or service class
    private Map<UUID, MoneyTransaction> transactionsMap = new ConcurrentHashMap<>();


    @Override
    public void start() {

        //TODO minimum set up of default accounts, transactions and customer
        setUpInitialData();

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        //Endpoint for post a funds transfer
        router.post("/api/transactions").handler(this::handleTransferMoney);
        //Endpoint to view the transactions log
        router.get("/api/transactions").handler(this::handleGetTransactions);
        //Start the server on port 6090
        vertx.createHttpServer().requestHandler(router::accept).listen(AppConstants.SERVER_PORT);
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

            log.debug("transactionRequestDTO=", transactionRequestDTO);

            //TODO validation check for null
            //TODO validation check on debit source account if amount be to be debited > balance amount of debit account
            if (!AccountValidator.doesAccountHaveEnoughFunds(transactionRequestDTO.getSourceAccount(), transactionRequestDTO.getTransferAmount())) {
                throw new InsufficientFundsException("Not enough funds to transfer");
            }

            MoneyTransaction moneyTransaction = null;

            //Manual locking can be used instead of synchronized block but this is used for simplicity
            //Optimistic locking is not prefered in this case but it might be a performance issue
            synchronized (transactionRequestDTO) {

                debitSourceAccount(transactionRequestDTO);
                creditDestinationAccount(transactionRequestDTO);

                //TODO refresh account balances in store

                moneyTransaction = new MoneyTransaction(transactionRequestDTO.getSourceAccount().getAccountNumber(),
                        transactionRequestDTO.getDestinationAccount().getAccountNumber(), transactionRequestDTO.getTransferAmount());

                log.debug("values after funds transfer =", transactionRequestDTO);
                log.debug("moneyTransaction values after funds transfer =", moneyTransaction.getTransactionId());

                transactionsMap.put(moneyTransaction.getTransactionId(), moneyTransaction);
            }

            //return moneytransaction object in case of 201 (successful creation)
            routingContext.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(moneyTransaction));
        } catch (InsufficientFundsException ise) {
            ErrorMessageResponse errorMessage = new ErrorMessageResponse(400, ise.getMessage(), LocalDateTime.now());
            log.error("Not enough funds to transfer, hence throwing user error response code");
            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(400)
                    .end(Json.encodePrettily(errorMessage));
        } catch (Exception e) {
            //Any other error apart from business exceptions would be a system error rather than an user error
            ErrorMessageResponse errorMessage = new ErrorMessageResponse(500, e.getMessage(), LocalDateTime.now());
            log.error("Generic server exception");
            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(500)
                    .end(Json.encodePrettily(errorMessage));
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
