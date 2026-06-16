package com.craftmmo.core.transaction;

@FunctionalInterface
public interface TransactionCallback<T> {
    T execute();
}
