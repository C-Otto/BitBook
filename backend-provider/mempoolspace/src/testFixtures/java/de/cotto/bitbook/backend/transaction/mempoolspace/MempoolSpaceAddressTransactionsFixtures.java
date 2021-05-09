package de.cotto.bitbook.backend.transaction.mempoolspace;

import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.TRANSACTION_HASH_4;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;

public class MempoolSpaceAddressTransactionsFixtures {
    public static final MempoolSpaceAddressTransactionsDto MEMPOOLSPACE_ADDRESS_DETAILS;
    public static final MempoolSpaceAddressTransactionsDto MEMPOOLSPACE_ADDRESS_UPDATED;

    static {
        MEMPOOLSPACE_ADDRESS_DETAILS = new MempoolSpaceAddressTransactionsDto(
                Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2)
        );
        MEMPOOLSPACE_ADDRESS_UPDATED = new MempoolSpaceAddressTransactionsDto(
                Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2, TRANSACTION_HASH_3, TRANSACTION_HASH_4)
        );
    }
}
