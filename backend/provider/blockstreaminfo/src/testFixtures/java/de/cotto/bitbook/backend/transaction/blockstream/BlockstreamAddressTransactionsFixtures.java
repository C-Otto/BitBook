package de.cotto.bitbook.backend.transaction.blockstream;

import java.util.Set;

import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_2;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_4;

public class BlockstreamAddressTransactionsFixtures {
    public static final BlockstreamAddressTransactionsDto BLOCKSTREAM_ADDRESS_DETAILS;
    public static final BlockstreamAddressTransactionsDto BLOCKSTREAM_ADDRESS_UPDATED;

    static {
        BLOCKSTREAM_ADDRESS_DETAILS = new BlockstreamAddressTransactionsDto(
                Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2)
        );
        BLOCKSTREAM_ADDRESS_UPDATED = new BlockstreamAddressTransactionsDto(
                Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2, TRANSACTION_HASH_3, TRANSACTION_HASH_4)
        );
    }
}
