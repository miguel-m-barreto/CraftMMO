package com.craftmmo.core.transaction;

public interface TransactionRunner {
    <T> T inTransaction(TransactionCallback<T> callback);
}
