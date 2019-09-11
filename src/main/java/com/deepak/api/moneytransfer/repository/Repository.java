package com.deepak.api.moneytransfer.repository;


import java.util.Collection;
import java.util.Optional;

//Base repository
public interface Repository<T, ID> {

    Optional<T> findById(ID id);

    void save(T entity);

    void delete(T entity);

    Collection<T> getAll();

}
