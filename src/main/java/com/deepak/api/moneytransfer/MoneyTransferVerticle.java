package com.deepak.api.moneytransfer;


import com.deepak.api.moneytransfer.service.MoneyTransferService;
import com.deepak.api.moneytransfer.service.MoneyTransferServiceImpl;
import com.deepak.api.moneytransfer.utils.AppConstants;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

import static com.deepak.api.moneytransfer.utils.AppConstants.*;

/*
 * This is the main class which boots the Vertx server and exposes the endpoints analogous to the Spring controller
 * */
@Slf4j
public class MoneyTransferVerticle extends AbstractVerticle {

    //Ideal scenario is to inject this service class
    private final MoneyTransferService moneyTransferService = new MoneyTransferServiceImpl();

    @Override
    public void start() {

        //This is needed for LocalDateTime and Json request mapping
        Json.mapper.registerModule(new JavaTimeModule());

        moneyTransferService.setUpInitialData();

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        //Endpoint to create the account
        router.post(API_FOR_ACCOUNTS).handler(moneyTransferService::handleCreateAccount);

        //Endpoint for post a funds transfer
        router.post(API_FOR_TRANSACTIONS).handler(moneyTransferService::handleTransferMoney);

        //Endpoint to view the transactions log
        router.get(API_FOR_GET_ACCOUNT_TRANSACTIONS).handler(moneyTransferService::handleTransactionsByAccountId);

        //Endpoint to view the accounts for a customer
        router.get(API_FOR_ACCOUNTS_FOR_A_CUSTOMER).handler(moneyTransferService::handleGetAccountsByCustomerId);//Todo the responses need not have the customer info because the search is for a customer

        //Endpoint to view all the accounts (possibly for an admin or a report)
        router.get(API_FOR_ACCOUNTS).handler(moneyTransferService::handleGetAccounts);



        //Start the server on port 6090
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(AppConstants.SERVER_PORT);
    }

}
