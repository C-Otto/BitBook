package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.Provider;
import de.cotto.bitbook.backend.model.ProviderException;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.Chain.BTC;

public abstract class SimpleAddressTransactionsProvider
        implements Provider<TransactionsRequestKey, AddressTransactions> {

    public SimpleAddressTransactionsProvider() {
        // default constructor
    }

    @Override
    public Optional<AddressTransactions> get(TransactionsRequestKey transactionsRequestKey) throws ProviderException {
        throwIfUnsupported(transactionsRequestKey);
        if (transactionsRequestKey.hasKnownAddressTransactions()) {
            return getCombined(transactionsRequestKey);
        }
        return getFromApi(transactionsRequestKey);
    }

    @Override
    public boolean isSupported(TransactionsRequestKey key) {
        return key.getChain() == BTC;
    }

    protected abstract Optional<AddressTransactions> getFromApi(TransactionsRequestKey transactionsRequestKey);

    private Optional<AddressTransactions> getCombined(TransactionsRequestKey transactionsRequestKey) {
        AddressTransactions knownTransactions = transactionsRequestKey.getAddressTransactions();
        return getFromApi(transactionsRequestKey)
                .map(knownTransactions::getCombined)
                .or(() -> Optional.of(knownTransactions));
    }
}
