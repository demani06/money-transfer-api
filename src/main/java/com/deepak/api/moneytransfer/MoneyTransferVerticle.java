package com.deepak.api.moneytransfer;


import com.deepak.api.moneytransfer.exception.InsufficientFundsException;
import com.deepak.api.moneytransfer.model.*;
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
    //TODO move this map to a repository or service class
    private Map<Long, Account> accountsMap = new ConcurrentHashMap<>();


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
        //Endpoint to view the accounts
        router.get("/api/accounts").handler(this::handleGetAccounts); //TODO this needs to be transfered to either customer/accounts or accounts/<account_id>
        //Start the server on port 6090
        vertx.createHttpServer().requestHandler(router::accept).listen(AppConstants.SERVER_PORT);
    }


    private void setUpInitialData() {
        //Basic test data for API

        Account sampleAccount1 = new Account(90909090L,606060L, new BigDecimal(500));
        Account sampleAccount2 = new Account(90909091L,606060L, new BigDecimal(400));
        Account sampleAccount3 = new Account(12222999L,606060L, new BigDecimal(800));
        Account sampleAccount4 = new Account(22222999L,606060L, new BigDecimal(900));
        accountsMap.put(sampleAccount1.getAccountNumber(),sampleAccount1);
        accountsMap.put(sampleAccount2.getAccountNumber(),sampleAccount2);
        accountsMap.put(sampleAccount3.getAccountNumber(),sampleAccount3);
        accountsMap.put(sampleAccount4.getAccountNumber(),sampleAccount4);

        //Transactions
        MoneyTransaction sampleTransaction = new MoneyTransaction(90909090L, 99999999L, new BigDecimal(100));
        transactionsMap.put(sampleTransaction.getTransactionId(), sampleTransaction);
    }

    private void handleGetTransactions(RoutingContext routingContext) {
        log.info("Start handleGetTransactions");

        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .setStatusCode(200)
                .end(Json.encodePrettily(transactionsMap.values()));
    }

    private void handleGetAccounts(RoutingContext routingContext) {
        log.info("Start handleGetAccounts");

        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .setStatusCode(200)
                .end(Json.encodePrettily(accountsMap.values()));
    }


    private void handleTransferMoney(RoutingContext routingContext) {

        log.info("Start handleTransferMoney");

        try {
            final TransactionRequestDTO transactionRequestDTO = Json.decodeValue(routingContext.getBodyAsString(), TransactionRequestDTO.class);

            log.debug("transactionRequestDTO=", transactionRequestDTO);

            if (!AccountValidator.isAccountValid(transactionRequestDTO.getSourceAccount(), accountsMap)) {
                throw new InvalidAccountException("Invalid account details"); //TODO add test case for the same
            }

            //TODO validation check for null
            //TODO validation check on debit source account if amount be to be debited > balance amount of debit account
            if (!AccountValidator.doesAccountHaveEnoughFunds(transactionRequestDTO.getSourceAccount(), transactionRequestDTO.getTransferAmount(), accountsMap)) {
                throw new InsufficientFundsException("Not enough funds to transfer");
            }

            MoneyTransaction moneyTransaction;

            //Manual locking can be used instead of synchronized block but this is used for simplicity
            //Optimistic locking is not prefered in this case but it might be a performance issue
            synchronized (transactionRequestDTO) {

                debitSourceAccount(transactionRequestDTO, accountsMap);
                creditDestinationAccount(transactionRequestDTO, accountsMap);

                //TODO refresh account balances in store
            /*    accountsMap.put(transactionRequestDTO.getSourceAccount().getAccountNumber(), transactionRequestDTO.getSourceAccount());
                accountsMap.put(transactionRequestDTO.getDestinationAccount().getAccountNumber(), transactionRequestDTO.getDestinationAccount());*/

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
        }catch (InvalidAccountException ex) {
            ErrorMessageResponse errorMessage = new ErrorMessageResponse(400, ex.getMessage(), LocalDateTime.now());
            log.error("Invalid account details, the account details are not there in map");
            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(400)
                    .end(Json.encodePrettily(errorMessage));
        }
        catch (Exception e) {
            //Any other error apart from business exceptions would be a system error rather than an user error
            ErrorMessageResponse errorMessage = new ErrorMessageResponse(500, e.getMessage(), LocalDateTime.now());
            log.error("Generic server exception");
            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(500)
                    .end(Json.encodePrettily(errorMessage));
        }
    }

    private void debitSourceAccount(TransactionRequestDTO transactionRequestDTO, Map<Long, Account> accountsMap) {

        Account debitAccount = accountsMap.get(transactionRequestDTO.getSourceAccount().getAccountNumber());

        log.info("debitAccount retrieved from Map = {}", debitAccount);

        BigDecimal newBalance = debitAccount.getBalance().subtract(transactionRequestDTO.getTransferAmount());

        //update account balance
        transactionRequestDTO.getSourceAccount().setBalance(newBalance);

        accountsMap.put(transactionRequestDTO.getSourceAccount().getAccountNumber(), transactionRequestDTO.getSourceAccount());

        log.info("debit source account step completed, refreshed account details ={}", transactionRequestDTO.getSourceAccount());

    }

    private void creditDestinationAccount(TransactionRequestDTO transactionRequestDTO, Map<Long, Account> accountsMap) {

        Account creditAccount = accountsMap.get(transactionRequestDTO.getDestinationAccount().getAccountNumber());

        log.info("creditAccount retrieved from Map = {}", creditAccount);

        BigDecimal newBalance = creditAccount.getBalance().add(transactionRequestDTO.getTransferAmount());
        //update account balance
        transactionRequestDTO.getDestinationAccount().setBalance(newBalance);
        accountsMap.put(transactionRequestDTO.getDestinationAccount().getAccountNumber(), transactionRequestDTO.getDestinationAccount());
        log.info("credit destination account step completed, refreshed account details ={}", transactionRequestDTO.getDestinationAccount());

    }

    private void sendError(int statusCode, HttpServerResponse response) {
        //TODO wrap this request?
        response.setStatusCode(statusCode).end();
    }

}
