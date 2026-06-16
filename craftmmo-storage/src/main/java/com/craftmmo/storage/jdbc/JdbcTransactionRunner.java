package com.craftmmo.storage.jdbc;

import com.craftmmo.core.transaction.TransactionCallback;
import com.craftmmo.core.transaction.TransactionRunner;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import javax.sql.DataSource;

public final class JdbcTransactionRunner implements TransactionRunner {
    private final DataSource dataSource;
    private final JdbcConnectionContext context;

    public JdbcTransactionRunner(DataSource dataSource, JdbcConnectionContext context) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
        this.context = Objects.requireNonNull(context, "context");
    }

    @Override
    public <T> T inTransaction(TransactionCallback<T> callback) {
        if (context.current().isPresent()) {
            return callback.execute();
        }
        try (Connection connection = dataSource.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            context.set(connection);
            try {
                T result = callback.execute();
                connection.commit();
                return result;
            } catch (RuntimeException ex) {
                connection.rollback();
                throw ex;
            } finally {
                context.clear();
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException ex) {
            throw new JdbcStorageException("Transaction failed", ex);
        }
    }
}
