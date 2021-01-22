package de.cotto.bitbook.backend.transaction.blockcypher;

import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.TRANSACTION_HASH_4;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;

public class BlockcypherAddressTransactionsFixtures {
    public static final BlockcypherAddressTransactionsDto BLOCKCYPHER_ADDRESS_DETAILS;
    public static final BlockcypherAddressTransactionsDto ADDRESS_DETAILS_INCOMPLETE;
    public static final BlockcypherAddressTransactionsDto ADDRESS_DETAILS_SECOND_PART;
    public static final BlockcypherAddressTransactionsDto BLOCKCYPHER_ADDRESS_UPDATE;

    private static final int LOWEST_COMPLETED_BLOCK_HEIGHT = 50;

    static {
        BLOCKCYPHER_ADDRESS_DETAILS = new BlockcypherAddressTransactionsDto(
                ADDRESS,
                Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2),
                false,
                0
        );
        ADDRESS_DETAILS_INCOMPLETE = new BlockcypherAddressTransactionsDto(
                ADDRESS,
                Set.of(TRANSACTION_HASH),
                true,
                LOWEST_COMPLETED_BLOCK_HEIGHT
        );
        ADDRESS_DETAILS_SECOND_PART = new BlockcypherAddressTransactionsDto(
                ADDRESS,
                Set.of(TRANSACTION_HASH_2),
                false,
                0
        );
        BLOCKCYPHER_ADDRESS_UPDATE = new BlockcypherAddressTransactionsDto(
                ADDRESS,
                Set.of(TRANSACTION_HASH_3, TRANSACTION_HASH_4),
                false,
                0
        );
    }
}
