package de.cotto.bitbook.backend.model;

import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS_2;
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
            LAST_CHECKED_AT_BLOCK_HEIGHT
    );
    public static final AddressTransactions ADDRESS_TRANSACTIONS_UPDATED = new AddressTransactions(
            ADDRESS,
            TRANSACTION_HASHES_2,
            LAST_CHECKED_AT_BLOCK_HEIGHT + 50
    );
    public static final AddressTransactions ADDRESS_TRANSACTIONS_2 = new AddressTransactions(
            ADDRESS_2,
            TRANSACTION_HASHES_2,
            LAST_CHECKED_AT_BLOCK_HEIGHT
    );
}
