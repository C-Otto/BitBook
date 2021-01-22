package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.Provider;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;

public interface AddressTransactionsProvider extends Provider<TransactionsRequestKey, AddressTransactions> {
}
