package com.deepak.api.moneytransfer.utils;

import com.deepak.api.moneytransfer.model.Account;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public class AccountValidator {

    public static boolean doesAccountHaveEnoughFunds(Account accountToBeChecked, BigDecimal amount) {
        log.info("Validating the source account for Zero and enough balance, accountToBeChecked = {} and amount = {}", accountToBeChecked, amount);
        log.info("accountToBeChecked.getBalance().longValue() = {}", accountToBeChecked.getBalance().longValue());
        log.info("amount.longValue = {}", amount.longValue());

        boolean doesAccountHaveEnoughFunds = false;

        if(accountToBeChecked.getBalance().compareTo(new BigDecimal(0)) > 0 && accountToBeChecked.getBalance().compareTo(amount) > 0){
            log.info("amount.longValue = {}", amount.longValue());
            doesAccountHaveEnoughFunds = true;
        }

        return doesAccountHaveEnoughFunds;

    }
}
