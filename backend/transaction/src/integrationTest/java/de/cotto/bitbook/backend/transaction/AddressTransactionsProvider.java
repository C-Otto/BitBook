package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.Provider;

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
