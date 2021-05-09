package de.cotto.bitbook.backend.transaction.blockchair;

import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.TRANSACTION_HASH_4;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;

public class BlockchairAddressTransactionsFixtures {
    public static final BlockchairAddressTransactionsDto BLOCKCHAIR_ADDRESS_DETAILS;
    public static final BlockchairAddressTransactionsDto BLOCKCHAIR_ADDRESS_UPDATED;

    static {
        BLOCKCHAIR_ADDRESS_DETAILS = new BlockchairAddressTransactionsDto(
                ADDRESS,
                Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2)
        );
        BLOCKCHAIR_ADDRESS_UPDATED = new BlockchairAddressTransactionsDto(
                ADDRESS,
                Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2, TRANSACTION_HASH_3, TRANSACTION_HASH_4)
        );
    }
}
