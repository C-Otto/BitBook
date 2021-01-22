package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.Provider;
import de.cotto.bitbook.backend.transaction.model.Transaction;

public abstract class TransactionProvider implements Provider<String, Transaction> {
    protected TransactionProvider() {
        // just used for tests
    }
}
