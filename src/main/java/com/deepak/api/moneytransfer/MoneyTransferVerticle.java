package com.deepak.api.moneytransfer;


import com.deepak.api.moneytransfer.exception.InsufficientFundsException;
import com.deepak.api.moneytransfer.exception.InvalidAccountException;
import com.deepak.api.moneytransfer.model.Account;
import com.deepak.api.moneytransfer.model.Customer;
import com.deepak.api.moneytransfer.model.MoneyTransaction;
import com.deepak.api.moneytransfer.repository.AccountRepositoryInMemoryImpl;
import com.deepak.api.moneytransfer.repository.AccountsRepository;
import com.deepak.api.moneytransfer.repository.TransactionRepository;
import com.deepak.api.moneytransfer.repository.TransactionRepositoryInMemoryImpl;
import com.deepak.api.moneytransfer.request.TransactionRequestDTO;
import com.deepak.api.moneytransfer.response.ErrorMessageResponse;
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
import java.util.Optional;
import java.util.UUID;

/*
 * This is the main class which boots the Vertx server and exposes the endpoints
 * */
@Slf4j
public class MoneyTransferVerticle extends AbstractVerticle {

    /*
    //TODO move this map to a repository or service class
    private Map<UUID, MoneyTransaction> transactionsMap = new ConcurrentHashMap<>();
    //TODO move this map to a repository or service class
    private Map<Long, Account> accountsMap = new ConcurrentHashMap<>();*/

    //private JDBCClient dbClient;

    private final AccountsRepository<Account, Long> accountsRepository = new AccountRepositoryInMemoryImpl();
    private final TransactionRepository<MoneyTransaction, UUID> transactionRepository = new TransactionRepositoryInMemoryImpl();

    @Override
    public void start() {

        setUpInitialData();

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        //Endpoint for post a funds transfer
        router.post("/api/transactions").handler(this::handleTransferMoney);
        //Endpoint to view the transactions log
        router.get("/api/accounts/:id/transactions/").handler(this::handleGetTransactionsByAccountId);
        //Endpoint to view the accounts
        router.get("/api/accounts").handler(this::handleGetAccounts); //TODO this needs to be transfered to either customer/accounts or accounts/<account_id>

        /*//Endpoint to create the account
        router.post("/api/accounts").handler(this::handleCreateAccounts);*/

        //Start the server on port 6090
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(AppConstants.SERVER_PORT);
    }


    private void setUpInitialData() {
        //Basic test data for API

        Customer customer1 = new Customer(UUID.randomUUID(), "Cunnan", "James", "cunnan.james@companya.com");
        Customer customer2 = new Customer(UUID.randomUUID(), "Sam", "Fox", "sam.fox@companyb.com");

        Account sampleAccount1 = new Account(90909090L, 606060L, new BigDecimal(500), customer1);
        Account sampleAccount2 = new Account(90909091L, 606060L, new BigDecimal(400), customer2);
        Account sampleAccount3 = new Account(12222999L, 606060L, new BigDecimal(800), customer1) ;
        Account sampleAccount4 = new Account(22222999L, 606060L, new BigDecimal(900), customer1);

        accountsRepository.save(sampleAccount1);
        accountsRepository.save(sampleAccount2);
        accountsRepository.save(sampleAccount3);
        accountsRepository.save(sampleAccount4);

        //Transactions
        MoneyTransaction sampleTransaction = new MoneyTransaction(90909090L, 99999999L, new BigDecimal(100));
        transactionRepository.save(sampleTransaction);
    }

    private void handleGetTransactionsByAccountId(RoutingContext routingContext) {

        String accountNum = routingContext.request()
                .getParam("id");

        log.info("Get transactions for account number = {} start ", accountNum);

        //Todo validate Account number for not null and a number

        if(null!=accountNum && accountNum.length()>0){
         routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .setStatusCode(200)
                .end(Json.encodePrettily(transactionRepository.getTransactionsByAccount(Long.valueOf(accountNum))));
        }
    }

    private void handleGetAccounts(RoutingContext routingContext) {
        log.info("Start handleGetAccounts");

        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .setStatusCode(200)
                .end(Json.encodePrettily(accountsRepository));
    }


    private void handleTransferMoney(RoutingContext routingContext) {

        log.info("Start handleTransferMoney");

        try {
            final TransactionRequestDTO transactionRequestDTO = Json.decodeValue(routingContext.getBodyAsString(), TransactionRequestDTO.class);

            log.debug("transactionRequestDTO=", transactionRequestDTO);

            if (!AccountValidator.isAccountValid(transactionRequestDTO.getSourceAccount(), accountsRepository)) {
                throw new InvalidAccountException("Invalid account details"); //TODO add test case for the same
            }

            //TODO validation check for null
            //TODO validation check on debit source account if amount be to be debited > balance amount of debit account
            if (!AccountValidator.doesAccountHaveEnoughFunds(transactionRequestDTO.getSourceAccount(), transactionRequestDTO.getTransferAmount(), accountsRepository)) {
                throw new InsufficientFundsException("Not enough funds to transfer");
            }

            MoneyTransaction moneyTransaction;

            //Manual locking can be used instead of synchronized block but this is used for simplicity
            //Optimistic locking is not preferred in this case but it might be a performance issue
            synchronized (transactionRequestDTO) {

                debitSourceAccount(transactionRequestDTO);
                creditDestinationAccount(transactionRequestDTO);

                //TODO refresh account balances in store
            /*    accountsMap.put(transactionRequestDTO.getSourceAccount().getAccountNumber(), transactionRequestDTO.getSourceAccount());
                accountsMap.put(transactionRequestDTO.getDestinationAccount().getAccountNumber(), transactionRequestDTO.getDestinationAccount());*/

              moneyTransaction =  this.transactionRepository.createTransaction(transactionRequestDTO.getSourceAccount(),
                        transactionRequestDTO.getDestinationAccount(), transactionRequestDTO.getTransferAmount());
            /*
                    moneyTransaction = new MoneyTransaction(transactionRequestDTO.getSourceAccount().getAccountNumber(),
                        transactionRequestDTO.getDestinationAccount().getAccountNumber(), transactionRequestDTO.getTransferAmount());

                log.debug("values after funds transfer =", transactionRequestDTO);

                transactionsMap.put(moneyTransaction.getTransactionId(), moneyTransaction);*/
                log.debug("moneyTransaction values after funds transfer =", moneyTransaction.getTransactionId());

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
        } catch (InvalidAccountException ex) {
            ErrorMessageResponse errorMessage = new ErrorMessageResponse(400, ex.getMessage(), LocalDateTime.now());
            log.error("Invalid account details, the account details are not there in map");
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

        Optional<Account> debitAccountOptional = this.accountsRepository.findById(transactionRequestDTO.getSourceAccount().getAccountNumber());

        if(debitAccountOptional.isPresent()) {

            Account debitAccount = debitAccountOptional.get();

            log.info("debitAccount retrieved from Map = {}", debitAccount);

            BigDecimal newBalance = debitAccount.getBalance().subtract(transactionRequestDTO.getTransferAmount());

            //update account balance
            debitAccount.setBalance(newBalance);

            this.accountsRepository.save(debitAccount);

            log.info("debit source account step completed, refreshed account details ={}", transactionRequestDTO.getSourceAccount());
        }
    }

    private void creditDestinationAccount(TransactionRequestDTO transactionRequestDTO) {

        Optional<Account> creditAccountOptional = this.accountsRepository.findById(transactionRequestDTO.getDestinationAccount().getAccountNumber());

        if(creditAccountOptional.isPresent()) {

            Account creditAccount = creditAccountOptional.get();

            log.info("creditAccount retrieved from Map = {}", creditAccount);

            BigDecimal newBalance = creditAccount.getBalance().add(transactionRequestDTO.getTransferAmount());
            //update account balance
            creditAccount.setBalance(newBalance);

            this.accountsRepository.save(creditAccount);

            log.info("credit destination account step completed, refreshed account details ={}", transactionRequestDTO.getDestinationAccount());
        }

    }


}
