# Threading

Paper main-thread startup must not perform blocking database or Flyway work.

Database startup runs on a bounded single-purpose executor. Shutdown closes resources and waits for executor termination.

Health commands expose lifecycle state and sanitized failure categories.
