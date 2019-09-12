package com.deepak.api.moneytransfer;

import com.deepak.api.moneytransfer.model.Account;
import com.deepak.api.moneytransfer.request.TransactionRequestDTO;
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

        Account account1 = new Account(12222999L, 606060L);
        Account account2 = new Account(22222999L, 606060L);

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
                        System.out.println("body=" + body.toJson().toString());
                        testContext.assertTrue(body.toJson().toString().contains("\"sourceAccountNumber\":12222999,\"destinationAccountNumber\":22222999,\"transactionAmount\":200}"));
                        testContext.assertTrue(body.toJson().toString().contains("{\"transactionId\":\""));
                        async.complete();
                    });
                })
                .write(json)
                .end();
    }

    @Test
    public void given_when_Money_Transfer_money_equals_balance_thenSuccess(TestContext testContext) {
        final Async async = testContext.async();

        Account account1 = new Account(12222999L, 606060L);
        Account account2 = new Account(22222999L, 606060L);

        //Request json
        final String json = Json.encodePrettily(new TransactionRequestDTO(account1, account2, new BigDecimal(800)));// account number has exactly 800

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
                        async.complete();
                    });
                })
                .write(json)
                .end();
    }


    @Test
    public void given_when_Money_Transfer_then_Failure_If_no_enough_funds(TestContext testContext) {
        final Async async = testContext.async();

        Account account1 = new Account(12222999L, 060606L, new BigDecimal(1500), null);
        Account account2 = new Account(22222999L, 060606L, new BigDecimal(500), null);

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
    public void given_when_Money_Transfer_then_Failure_If_invalid_Source_account_no(TestContext testContext) {
        final Async async = testContext.async();

        Account account1 = new Account(89L, 060606L, new BigDecimal(1500), null);
        Account account2 = new Account(22222999L, 060606L, new BigDecimal(500), null);

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
                        System.out.println("body=" + body.toJson().toString());
                        testContext.assertTrue(body.toJson().toString().contains("\"statusCode\":400,\"exceptionMessage\":\"Invalid account details, source account number is null or has invalid length\""));
                        async.complete();
                    });
                })
                .write(json)
                .end();
    }


    @Test
    public void given_when_Money_Transfer_then_Failure_If_invalid_destination_account_no(TestContext testContext) {
        final Async async = testContext.async();

        Account account1 = new Account(12222999L, 060606L, new BigDecimal(1500), null);
        Account account2 = new Account(11L, 060606L, new BigDecimal(500), null);

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
                        System.out.println("body=" + body.toJson().toString());
                        testContext.assertTrue(body.toJson().toString().contains("\"statusCode\":400,\"exceptionMessage\":\"Invalid account details, destination account number is null or has invalid length\","));
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
                .get(AppConstants.SERVER_PORT, "localhost", "/api/accounts/90909090/transactions/")
                .putHeader("content-type", "application/json")
                .handler(response -> {
                    testContext.assertEquals(response.statusCode(), 200);
                    testContext.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        System.out.println("body=" + body.toJson().toString());
                        testContext.assertTrue(body.toJson().toString().contains("\"sourceAccountNumber\":90909090,\"destinationAccountNumber\":99999999,\"transactionAmount\":100"));
                        async.complete();
                    });
                })
                .end();
    }

    @Test
    public void given_when_call_get_Transactions_thenReturn_400_for_Invalid_accountNumber(TestContext testContext) {
        final Async async = testContext.async();

        vertx.createHttpClient()
                .get(AppConstants.SERVER_PORT, "localhost", "/api/accounts/90909090a/transactions/")
                .putHeader("content-type", "application/json")
                .handler(response -> {
                    testContext.assertEquals(response.statusCode(), 400);
                    testContext.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        testContext.assertTrue(body.toString().contains("Invalid Account number"));
                        async.complete();
                    });
                })
                .end();
    }

    @Test
    public void given_when_call_get_Transactions_then_Return_204_for_accountNumber_If_No_transactions(TestContext testContext) {
        final Async async = testContext.async();

        vertx.createHttpClient()
                .get(AppConstants.SERVER_PORT, "localhost", "/api/accounts/22222999/transactions/")
                .putHeader("content-type", "application/json")
                .handler(response -> {
                    testContext.assertEquals(response.statusCode(), 204);
                    testContext.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        //testContext.assertTrue(body.toString().contains("Invalid Account number"));
                        async.complete();
                    });
                })
                .end();
    }

    @Test
    public void given_when_call_get_Accounts_forCustomer_then_return_success(TestContext testContext) {
        final Async async = testContext.async();

        vertx.createHttpClient()
                .get(AppConstants.SERVER_PORT, "localhost", "/api/customer/abcdef/accounts")
                .putHeader("content-type", "application/json")
                .handler(response -> {
                    testContext.assertEquals(response.statusCode(), 200);
                    testContext.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        System.out.println("body = " + body);
                        System.out.println("body jsonobject = " + body.toJson());

                        testContext.assertTrue(body.toJson().toString().contains("{\"accountNumber\":\"90909090\",\"sortCode\":\"606060\",\"balance\":\"500\"},{\"accountNumber\":\"22222999\",\"sortCode\":\"606060\",\"balance\":\"900\"},{\"accountNumber\":\"12222999\",\"sortCode\":\"606060\",\"balance\":\"800\"}"));
                        async.complete();
                    });
                })
                .end();
    }

    @Test
    public void given_when_call_get_Accounts_forCustomer_then_return_400(TestContext testContext) {
        final Async async = testContext.async();

        vertx.createHttpClient()
                .get(AppConstants.SERVER_PORT, "localhost", "/api/customer/@@@abcdef/accounts")
                .putHeader("content-type", "application/json")
                .handler(response -> {
                    testContext.assertEquals(response.statusCode(), 400);
                    testContext.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        System.out.println("body = " + body);
                        testContext.assertTrue(body.toString().contains("Invalid Customer Id"));
                        async.complete();
                    });
                })
                .end();
    }
}

