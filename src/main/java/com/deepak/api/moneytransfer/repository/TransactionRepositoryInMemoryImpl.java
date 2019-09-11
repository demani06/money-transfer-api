package com.deepak.api.moneytransfer.repository;

import com.deepak.api.moneytransfer.model.Account;
import com.deepak.api.moneytransfer.model.MoneyTransaction;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class TransactionRepositoryInMemoryImpl implements TransactionRepository<MoneyTransaction, UUID> {

    private Map<UUID, MoneyTransaction> transactionsMap = new ConcurrentHashMap<>();

    @Override
    public MoneyTransaction createTransaction(Account debitAccount, Account creditAccount, BigDecimal amount) {
        MoneyTransaction moneyTransaction = new MoneyTransaction(debitAccount.getAccountNumber(),
                creditAccount.getAccountNumber(), amount);

        //save the transaction to the map
        save(moneyTransaction);

        return moneyTransaction;
    }

    @Override
    public Set<MoneyTransaction> getTransactionsByAccount(Long accountNumber) {

        log.info("transactionMap = {}", transactionsMap);
        log.info("Search the map with account number - {}", accountNumber);

        if (transactionsMap.size() == 0)
            return Collections.EMPTY_SET;

        return transactionsMap
                .values()
                .stream()
                .filter(account -> account.getSourceAccountNumber().compareTo(accountNumber)==0 || account.getDestinationAccountNumber().compareTo(accountNumber)==0)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<MoneyTransaction> findById(UUID uuid) {
        return Optional.ofNullable(transactionsMap.get(uuid));
    }

    @Override
    public void save(MoneyTransaction moneyTransaction) {
        transactionsMap.put(moneyTransaction.getTransactionId(), moneyTransaction);

    }

    @Override
    public void delete(MoneyTransaction entity) {
        //Todo implementation for this method
    }

    @Override
    public Collection<MoneyTransaction> getAll() {
        return Collections.emptyList(); //not in scope at the moment
    }
}
