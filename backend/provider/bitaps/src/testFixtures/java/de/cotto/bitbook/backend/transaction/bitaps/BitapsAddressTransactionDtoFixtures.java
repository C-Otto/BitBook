package de.cotto.bitbook.backend.transaction.bitaps;

import java.util.Set;

import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_2;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_4;

public class BitapsAddressTransactionDtoFixtures {
    public static final BitapsAddressTransactionsDto BITAPS_ADDRESS_TRANSACTIONS;
    public static final BitapsAddressTransactionsDto BITAPS_TRANSACTIONS_UPDATED;

    static {
        BITAPS_ADDRESS_TRANSACTIONS = new BitapsAddressTransactionsDto(
                Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2)
        );

        BITAPS_TRANSACTIONS_UPDATED = new BitapsAddressTransactionsDto(
                Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2, TRANSACTION_HASH_3, TRANSACTION_HASH_4)
        );
    }
}
