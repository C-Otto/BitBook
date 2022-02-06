package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.Provider;

public interface AddressTransactionsProvider extends Provider<TransactionsRequestKey, AddressTransactions> {
}
