package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.HashAndChain;
import de.cotto.bitbook.backend.model.Provider;
import de.cotto.bitbook.backend.model.Transaction;

public abstract class TransactionProvider implements Provider<HashAndChain, Transaction> {
    protected TransactionProvider() {
        // just used for tests
    }
}
