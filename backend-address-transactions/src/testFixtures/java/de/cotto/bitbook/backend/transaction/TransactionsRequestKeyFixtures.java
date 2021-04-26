package de.cotto.bitbook.backend.transaction;

import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;

public class TransactionsRequestKeyFixtures {
    public static final TransactionsRequestKey TRANSACTIONS_REQUEST_KEY =
            new TransactionsRequestKey(ADDRESS, BLOCK_HEIGHT);
    public static final AddressTransactionsRequest ADDRESS_TRANSACTIONS_REQUEST =
            AddressTransactionsRequest.create(TRANSACTIONS_REQUEST_KEY, STANDARD);
    public static final AddressTransactionsRequest ADDRESS_TRANSACTIONS_LOWEST =
            AddressTransactionsRequest.create(TRANSACTIONS_REQUEST_KEY, LOWEST);
}
