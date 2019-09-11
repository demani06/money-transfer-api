package com.deepak.api.moneytransfer.utils;

import com.deepak.api.moneytransfer.model.Account;
import com.deepak.api.moneytransfer.repository.AccountsRepository;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class AccountValidator {
    //todo delete the obsolete method
    public static boolean doesAccountHaveEnoughFunds(Account accountToBeChecked, BigDecimal amount, Map<Long, Account> accountsMap) {
        log.info("Validating the source account for Zero and enough balance, accountToBeChecked = {} and amount = {}", accountToBeChecked, amount);

        Account debitAccount = accountsMap.get(accountToBeChecked.getAccountNumber());

        boolean doesAccountHaveEnoughFunds = false;

        if (debitAccount.getBalance().compareTo(new BigDecimal(0)) > 0 && debitAccount.getBalance().compareTo(amount) >= 0) {
            doesAccountHaveEnoughFunds = true;
        }

        log.info("Account validation for funds check completed and result = {}", doesAccountHaveEnoughFunds);

        return doesAccountHaveEnoughFunds;
    }

    public static boolean doesAccountHaveEnoughFunds(Account accountToBeChecked, BigDecimal amount, AccountsRepository accountsRepository) {
        log.info("Validating the source account for Zero and enough balance, accountToBeChecked = {} and amount = {}", accountToBeChecked, amount);

        final Optional<Account> debitAccountOptional  = getAccountByAccountNumber(accountToBeChecked, accountsRepository);

        boolean doesAccountHaveEnoughFunds = false;

        if(debitAccountOptional.isPresent()) {

            Account debitAccount = debitAccountOptional.get();

            if (debitAccount.getBalance().compareTo(new BigDecimal(0)) > 0 && debitAccount.getBalance().compareTo(amount) >= 0) {
                doesAccountHaveEnoughFunds = true;
            }

        }
        log.info("Account validation for funds check completed and result = {}", doesAccountHaveEnoughFunds);

        return doesAccountHaveEnoughFunds;
    }


    public static boolean isAccountValid(Account sourceAccount, AccountsRepository accountsRepository) {
        boolean isAccountValid = false;

        log.info("Checking if account number provided exists");

        final Optional<Account> accountOptional  = getAccountByAccountNumber(sourceAccount, accountsRepository);

        log.info("accountOptional = {}", accountOptional);

        if(accountOptional.isPresent()){
            isAccountValid = true;
        }

        return isAccountValid;
    }

    private static Optional getAccountByAccountNumber(Account sourceAccount, AccountsRepository accountsRepository) {
        return accountsRepository.findById(sourceAccount.getAccountNumber());
    }
}
