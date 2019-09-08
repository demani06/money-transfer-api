package com.deepak.api.moneytransfer;

import com.deepak.api.moneytransfer.model.Account;
import com.deepak.api.moneytransfer.model.TransactionRequestDTO;
import com.deepak.api.moneytransfer.utils.AppConstants;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;

@Slf4j
@RunWith(VertxUnitRunner.class)
public class MoneyTransferServiceIntegrationTest {

    private Vertx vertx;

    @Before
    public void setup(TestContext testContext) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(MoneyTransferVerticle.class.getName(), testContext.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext testContext) {
        vertx.close(testContext.asyncAssertSuccess());
    }

    @Test
    public void given_when_Money_Transfer_thenSuccess(TestContext testContext) {
        final Async async = testContext.async();

        Account account1 = new Account(12222999L, 060606L, new BigDecimal(1500));
        Account account2 = new Account(22222999L, 060606L, new BigDecimal(500));

        //Request json
        final String json = Json.encodePrettily(new TransactionRequestDTO(account1, account2, new BigDecimal(200)));

        final String length = Integer.toString(json.length());

        vertx.createHttpClient()
                .post(AppConstants.SERVER_PORT, "localhost", "/api/transactions")
                .putHeader("content-type", "application/json")
                .putHeader("content-length", length)
                .handler(response -> {
                    testContext.assertEquals(response.statusCode(), 201);
                    testContext.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        System.out.println("body=" + body);
                        //TODO call the get accounts to see the refreshed balance and assert the balances of both accounts
                        async.complete();
                    });
                })
                .write(json)
                .end();
    }


    @Test
    public void given_when_Money_Transfer_then_Failure_If_no_enough_funds(TestContext testContext) {
        final Async async = testContext.async();

        Account account1 = new Account(12222999L, 060606L, new BigDecimal(1500));
        Account account2 = new Account(22222999L, 060606L, new BigDecimal(500));

        //Request json
        final String json = Json.encodePrettily(new TransactionRequestDTO(account1, account2, new BigDecimal(2200)));

        final String length = Integer.toString(json.length());

        vertx.createHttpClient()
                .post(AppConstants.SERVER_PORT, "localhost", "/api/transactions")
                .putHeader("content-type", "application/json")
                .putHeader("content-length", length)
                .handler(response -> {
                    log.info("resp in test case = {}", response);
                    log.info("Status code in test case = {}", response.statusCode());
                    testContext.assertEquals(response.statusCode(), 400);
                    testContext.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        System.out.println("body=" + body);
                        async.complete();
                    });
                })
                .write(json)
                .end();
    }

    @Test
    public void given_when_call_get_Transactions_thenSuccess(TestContext testContext) {
        final Async async = testContext.async();

        //Given get api to see transactions
        //when there are some transactions in map (in memory store)
        //Added a default transaction in initial data
        //then

        vertx.createHttpClient()
                .get(AppConstants.SERVER_PORT, "localhost", "/api/transactions")
                .putHeader("content-type", "application/json")
                .handler(response -> {
                    testContext.assertEquals(response.statusCode(), 200);
                    testContext.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        System.out.println("body=" + body);
                        //TODO call the get accounts to see the refreshed balance and assert the balances of both accounts
                        async.complete();
                    });
                })
                .end();
    }
}

