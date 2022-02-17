package de.cotto.bitbook.backend.transaction;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;

public class TransactionsRequestKeyFixtures {
    public static final TransactionsRequestKey TRANSACTIONS_REQUEST_KEY =
            new TransactionsRequestKey(ADDRESS, BTC, BLOCK_HEIGHT);
    public static final AddressTransactionsRequest ADDRESS_TRANSACTIONS_REQUEST =
            AddressTransactionsRequest.create(TRANSACTIONS_REQUEST_KEY, STANDARD);
    public static final AddressTransactionsRequest ADDRESS_TRANSACTIONS_LOWEST =
            AddressTransactionsRequest.create(TRANSACTIONS_REQUEST_KEY, LOWEST);
}
