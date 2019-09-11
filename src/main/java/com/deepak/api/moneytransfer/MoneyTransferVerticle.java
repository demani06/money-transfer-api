package com.deepak.api.moneytransfer;


import com.deepak.api.moneytransfer.service.MoneyTransferService;
import com.deepak.api.moneytransfer.service.MoneyTransferServiceImpl;
import com.deepak.api.moneytransfer.utils.AppConstants;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

/*
 * This is the main class which boots the Vertx server and exposes the endpoints analogous to the Spring controller
 * */
@Slf4j
public class MoneyTransferVerticle extends AbstractVerticle {

    //Ideal scenario is to inject this service class
    private final MoneyTransferService moneyTransferService = new MoneyTransferServiceImpl();

    @Override
    public void start() {

        moneyTransferService.setUpInitialData();

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        //Endpoint for post a funds transfer
        router.post("/api/transactions").handler(moneyTransferService::handleTransferMoney);
        //Endpoint to view the transactions log
        router.get("/api/accounts/:id/transactions/").handler(moneyTransferService::handleTransactionsByAccountId);
        //Endpoint to view the accounts
        router.get("/api/accounts").handler(moneyTransferService::handleGetAccounts);

        /*//Endpoint to create the account
        router.post("/api/accounts").handler(this::handleCreateAccounts);*/

        //Start the server on port 6090
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(AppConstants.SERVER_PORT);
    }

}
