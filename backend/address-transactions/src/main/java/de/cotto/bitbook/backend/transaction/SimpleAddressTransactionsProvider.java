package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.Provider;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;

import java.util.Optional;

public abstract class SimpleAddressTransactionsProvider
        implements Provider<TransactionsRequestKey, AddressTransactions> {

    public SimpleAddressTransactionsProvider() {
        // default constructor
    }

    @Override
    public Optional<AddressTransactions> get(TransactionsRequestKey transactionsRequestKey) {
        if (transactionsRequestKey.hasKnownAddressTransactions()) {
            return getCombined(transactionsRequestKey);
        }
        return getFromApi(transactionsRequestKey);
    }

    protected abstract Optional<AddressTransactions> getFromApi(TransactionsRequestKey transactionsRequestKey);

    private Optional<AddressTransactions> getCombined(TransactionsRequestKey transactionsRequestKey) {
        AddressTransactions knownTransactions = transactionsRequestKey.getAddressTransactions();
        return getFromApi(transactionsRequestKey)
                .map(knownTransactions::getCombined)
                .or(() -> Optional.of(knownTransactions));
    }
}
