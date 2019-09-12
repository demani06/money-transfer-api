package com.deepak.api.moneytransfer.repository;

import com.deepak.api.moneytransfer.model.Customer;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CustomerRepositoryInMemoryImpl implements CustomerRepository<Customer, String> {

    private Map<String, Customer> customersMap = new ConcurrentHashMap<>();

    @Override
    public Optional<Customer> findById(String customerId) {
        log.info("CustomersMap = {}", customersMap);
        log.info("Searching customer id - {} from CustomersMap size = {}",customerId, customersMap.size());
        return customersMap.values().stream().filter(customer -> customer.getCustomerId().equalsIgnoreCase(customerId)).findFirst();
    }

    @Override
    public void save(Customer customer) {
        customersMap.putIfAbsent(customer.getEmailAddress(), customer);
    }

    @Override
    public void delete(Customer entity) {

    }

    @Override
    public Collection<Customer> getAll() {
        return null;
    }

    @Override
    public Optional<Customer> findByEmailAddress(String emailAddress) {
        log.info("CustomersMap = {}", customersMap);
        log.info("Searching customer emailAddress - {} from CustomersMap size = {}", emailAddress,  customersMap.size());
        return customersMap.values().stream().filter(customer -> customer.getEmailAddress().equalsIgnoreCase(emailAddress)).findFirst();
    }
}
