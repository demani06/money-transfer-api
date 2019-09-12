package com.deepak.api.moneytransfer.repository;

import com.deepak.api.moneytransfer.model.Account;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This class is in memory version of Account Repo, use map for making the DAO services simple
 */

@Slf4j
public class AccountRepositoryInMemoryImpl implements AccountsRepository<Account, Long> {

    private Map<Long, Account> accountsMap = new ConcurrentHashMap<>();

    @Override
    public Optional<Account> findById(Long accountNumberTobeRetrieved) {
        log.info("accountsMap size = {} and account number to be searched :{}", accountsMap.size(), accountNumberTobeRetrieved);
        return accountsMap.values().stream().filter(account -> account.getAccountNumber().compareTo(accountNumberTobeRetrieved) == 0).findFirst();
    }

    @Override
    public void save(Account account) {
        log.info("saving a new Account, account = {}", account);
        accountsMap.put(account.getAccountNumber(), account);
    }

    @Override
    public void delete(Account account) {
        //Implementation for this out of scope
    }

    @Override
    public Collection<Account> getAll() {
        return accountsMap.values();
    }


    @Override
    public Collection<Account> findAllByCustomerId(String customerId) {
        log.info("accountsMap size = {} and customerId to be searched :{}", accountsMap.size(), customerId);
        //The assumption is that by this time it comes here the basic validation is done
        return accountsMap.values().stream().filter(account -> account.getCustomer().getCustomerId().equalsIgnoreCase(customerId)).collect(Collectors.toSet());
    }
}
