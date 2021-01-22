package de.cotto.bitbook.backend.transaction;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;

public class TransactionsRequestKeyFixtures {
    public static final TransactionsRequestKey TRANSACTIONS_REQUEST_KEY =
            new TransactionsRequestKey(ADDRESS, BLOCK_HEIGHT);
    public static final AddressTransactionsRequest ADDRESS_TRANSACTIONS_REQUEST =
            AddressTransactionsRequest.forStandardPriority(TRANSACTIONS_REQUEST_KEY);
    public static final AddressTransactionsRequest ADDRESS_TRANSACTIONS_LOWEST =
            AddressTransactionsRequest.forLowestPriority(TRANSACTIONS_REQUEST_KEY);
}
