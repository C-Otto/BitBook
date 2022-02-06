package de.cotto.bitbook.backend.model;

import java.util.Set;

import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH_2;

public class AddressTransactionsFixtures {
    public static final String ADDRESS = "1DEP8i3QJCsomS4BSMY2RpU1upv62aGvhD";
    public static final String ADDRESS_2 = "191sNkKTG8pzUsNgZYKo7DH2odg39XDAGo";
    public static final String ADDRESS_3 = "bc1qwqdg6squsna38e46795at95yu9atm8azzmyvckulcc7kytlcckxswvvzej";
    public static final String TRANSACTION_HASH_3 = "0003";
    public static final String TRANSACTION_HASH_4 = "0004";
    public static final Set<String> TRANSACTION_HASHES = Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2);
    public static final Set<String> TRANSACTION_HASHES_2 =
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
