package com.deepak.api.moneytransfer.repository;

import java.util.Optional;

public interface CustomerRepository<Customer, String> extends Repository<Customer, String> {

    Optional<Customer> findByEmailAddress(String emailAddress);
}
