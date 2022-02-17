package de.cotto.bitbook.backend.model;

import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS_2;
import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BSV;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_2;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_4;

public class AddressTransactionsFixtures {
    public static final Set<TransactionHash> TRANSACTION_HASHES = Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2);
    public static final Set<TransactionHash> TRANSACTION_HASHES_2 =
            Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2, TRANSACTION_HASH_3, TRANSACTION_HASH_4);
    public static final int LAST_CHECKED_AT_BLOCK_HEIGHT = 678_123;
    public static final AddressTransactions ADDRESS_TRANSACTIONS = new AddressTransactions(
            ADDRESS,
            TRANSACTION_HASHES,
            LAST_CHECKED_AT_BLOCK_HEIGHT,
            BTC
    );
    public static final AddressTransactions ADDRESS_TRANSACTIONS_BCH = new AddressTransactions(
            ADDRESS,
            TRANSACTION_HASHES,
            LAST_CHECKED_AT_BLOCK_HEIGHT,
            BCH
    );
    public static final AddressTransactions ADDRESS_TRANSACTIONS_BSV = new AddressTransactions(
            ADDRESS,
            TRANSACTION_HASHES,
            LAST_CHECKED_AT_BLOCK_HEIGHT,
            BSV
    );
    public static final AddressTransactions ADDRESS_TRANSACTIONS_UPDATED = new AddressTransactions(
            ADDRESS,
            TRANSACTION_HASHES_2,
            LAST_CHECKED_AT_BLOCK_HEIGHT + 50,
            BTC
    );
    public static final AddressTransactions ADDRESS_TRANSACTIONS_2 = new AddressTransactions(
            ADDRESS_2,
            TRANSACTION_HASHES_2,
            LAST_CHECKED_AT_BLOCK_HEIGHT,
            BTC
    );
}
