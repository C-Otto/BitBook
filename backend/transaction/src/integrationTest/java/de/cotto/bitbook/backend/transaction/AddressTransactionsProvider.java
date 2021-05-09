package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.Provider;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;

import java.util.Optional;

public class AddressTransactionsProvider implements Provider<TransactionsRequestKey, AddressTransactions> {
    public AddressTransactionsProvider() {
        // default constructor
    }

    @Override
    public String getName() {
        return "for test";
    }

    @Override
    public Optional<AddressTransactions> get(TransactionsRequestKey key) {
        return Optional.empty();
    }
}
