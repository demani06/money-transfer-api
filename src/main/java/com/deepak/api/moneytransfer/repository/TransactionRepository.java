package com.deepak.api.moneytransfer.repository;

import com.deepak.api.moneytransfer.model.Account;
import com.deepak.api.moneytransfer.model.MoneyTransaction;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public interface TransactionRepository<M, U> extends Repository<MoneyTransaction, UUID> {

    MoneyTransaction createTransaction(Account debitAccount, Account creditAccount, BigDecimal amount);

    Set<MoneyTransaction> getTransactionsByAccount(Long accountNumber);

}
