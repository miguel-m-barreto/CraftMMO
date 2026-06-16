package com.craftmmo.testkit;

import com.craftmmo.core.transaction.TransactionCallback;
import com.craftmmo.core.transaction.TransactionRunner;

public final class InMemoryTransactionRunner implements TransactionRunner {
    @Override
    public <T> T inTransaction(TransactionCallback<T> callback) {
        return callback.execute();
    }
}
