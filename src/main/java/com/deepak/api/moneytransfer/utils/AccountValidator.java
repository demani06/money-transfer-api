package com.deepak.api.moneytransfer.utils;

import com.deepak.api.moneytransfer.model.Account;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
public class AccountValidator {

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

    public static boolean isAccountValid(Account sourceAccount, Map<Long, Account> accountsMap) {
        boolean isAccountValid = false;

        if(accountsMap.containsKey(sourceAccount.getAccountNumber())){
            isAccountValid = true;
        }

        return isAccountValid;
    }
}
