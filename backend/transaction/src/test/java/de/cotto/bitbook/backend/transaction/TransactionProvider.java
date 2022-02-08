package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.Provider;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;

public abstract class TransactionProvider implements Provider<TransactionHash, Transaction> {
    protected TransactionProvider() {
        // just used for tests
    }
}
