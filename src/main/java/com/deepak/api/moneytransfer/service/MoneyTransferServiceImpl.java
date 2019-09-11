package com.deepak.api.moneytransfer.service;

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
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/*
 * Extra service class which abstracts the service method and calls to the DAO layer
 * */
@Slf4j
public class MoneyTransferServiceImpl implements MoneyTransferService {

    private final AccountsRepository<Account, Long> accountsRepository = new AccountRepositoryInMemoryImpl();
    private final TransactionRepository<MoneyTransaction, UUID> transactionRepository = new TransactionRepositoryInMemoryImpl();

    @Override
    public void setUpInitialData() {

        //Basic test data for API, in  an ideal world this would create a DB table and insert the master data

        Customer customer1 = new Customer(UUID.randomUUID(), "Cunnan", "James", "cunnan.james@companya.com");
        Customer customer2 = new Customer(UUID.randomUUID(), "Sam", "Fox", "sam.fox@companyb.com");

        Account sampleAccount1 = new Account(90909090L, 606060L, new BigDecimal(500), customer1);
        Account sampleAccount2 = new Account(90909091L, 606060L, new BigDecimal(400), customer2);
        Account sampleAccount3 = new Account(12222999L, 606060L, new BigDecimal(800), customer1);
        Account sampleAccount4 = new Account(22222999L, 606060L, new BigDecimal(900), customer1);

        accountsRepository.save(sampleAccount1);
        accountsRepository.save(sampleAccount2);
        accountsRepository.save(sampleAccount3);
        accountsRepository.save(sampleAccount4);

        //Transactions
        MoneyTransaction sampleTransaction = new MoneyTransaction(90909090L, 99999999L, new BigDecimal(100));
        transactionRepository.save(sampleTransaction);
    }

    @Override
    public void handleTransactionsByAccountId(RoutingContext routingContext) {
        String accountNum = routingContext.request()
                .getParam("id");

        log.info("Get transactions for account number = {} start ", accountNum);

        if (!AccountValidator.validateAccountNumber(accountNum)) { //means that it is either null or empty string or not a number
            routeResponse(routingContext, 400, "Invalid Account number");
        } else {
            Set<MoneyTransaction> transactionsByAccount = transactionRepository.getTransactionsByAccount(Long.valueOf(accountNum));

            if(transactionsByAccount.isEmpty()){
                routeResponse(routingContext, 204, "No Transactions"); //for empty collection return 204
            }
            else{
                routeResponse(routingContext, 200, transactionsByAccount);
            }

        }
    }

    private void routeResponse(RoutingContext routingContext, int statusCode, Object o) {
        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .setStatusCode(statusCode)
                .end(Json.encodePrettily(o));
    }

    @Override
    public void handleGetAccounts(RoutingContext routingContext) {
        log.info("Start handleGetAccounts");
        routeResponse(routingContext, 200, accountsRepository.getAll());
    }

    @Override
    public void handleTransferMoney(RoutingContext routingContext) {
        log.info("Start handleTransferMoney");

        try {
            final TransactionRequestDTO transactionRequestDTO = Json.decodeValue(routingContext.getBodyAsString(), TransactionRequestDTO.class);

            log.debug("transactionRequestDTO=", transactionRequestDTO);

            if (!AccountValidator.isAccountValid(transactionRequestDTO.getSourceAccount(), accountsRepository)) {
                throw new InvalidAccountException("Invalid account details"); //TODO add test case for the same
            }

            if (!AccountValidator.doesAccountHaveEnoughFunds(transactionRequestDTO.getSourceAccount(), transactionRequestDTO.getTransferAmount(), accountsRepository)) {
                throw new InsufficientFundsException("Not enough funds to transfer");
            }

            MoneyTransaction moneyTransaction;

            //Manual locking can be used instead of synchronized block but this is used for simplicity
            //Optimistic locking is not preferred in this case but it might be a performance issue
            //This might not be not needed at all in case of vertx server if we are strictly following non blocking IO
            synchronized (transactionRequestDTO) {

                debitSourceAccount(transactionRequestDTO);
                creditDestinationAccount(transactionRequestDTO);

                moneyTransaction = this.transactionRepository.createTransaction(transactionRequestDTO.getSourceAccount(),
                        transactionRequestDTO.getDestinationAccount(), transactionRequestDTO.getTransferAmount());

                log.debug("moneyTransaction values after funds transfer =", moneyTransaction.getTransactionId());

            }

            //return moneytransaction object in case of 201 (successful creation)
            routeResponse(routingContext, 201, moneyTransaction);

        } catch (InsufficientFundsException ise) {

            ErrorMessageResponse errorMessage = new ErrorMessageResponse(400, ise.getMessage(), LocalDateTime.now());
            log.error("Not enough funds to transfer, hence throwing user error response code");
            routeResponse(routingContext, 400, errorMessage);

        } catch (InvalidAccountException ex) {

            ErrorMessageResponse errorMessage = new ErrorMessageResponse(400, ex.getMessage(), LocalDateTime.now());
            log.error("Invalid account details, the account details are not there in map");
            routeResponse(routingContext, 400, errorMessage);

        } catch (Exception e) {

            //Any other error apart from business exceptions would be a system error rather than an user error
            ErrorMessageResponse errorMessage = new ErrorMessageResponse(500, e.getMessage(), LocalDateTime.now());
            log.error("Generic server exception");
            routeResponse(routingContext, 400, errorMessage);

        }
    }

    @Override
    public void debitSourceAccount(TransactionRequestDTO transactionRequestDTO) {

        Optional<Account> debitAccountOptional = this.accountsRepository.findById(transactionRequestDTO.getSourceAccount().getAccountNumber());

        if (debitAccountOptional.isPresent()) {

            Account debitAccount = debitAccountOptional.get();

            log.info("debitAccount retrieved from Map = {}", debitAccount);

            BigDecimal newBalance = debitAccount.getBalance().subtract(transactionRequestDTO.getTransferAmount());

            //update account balance
            debitAccount.setBalance(newBalance);

            this.accountsRepository.save(debitAccount);

            log.info("debit source account step completed, refreshed account details ={}", transactionRequestDTO.getSourceAccount());
        }
    }

    @Override
    public void creditDestinationAccount(TransactionRequestDTO transactionRequestDTO) {
        Optional<Account> creditAccountOptional = this.accountsRepository.findById(transactionRequestDTO.getDestinationAccount().getAccountNumber());

        if (creditAccountOptional.isPresent()) {

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
