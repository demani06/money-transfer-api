package com.deepak.api.moneytransfer.repository;

import java.util.Collection;

public interface AccountsRepository<Account, Long> extends Repository<Account,Long> {

    Collection<Account> findAllByCustomerId(String customerId);

}
