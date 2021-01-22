package de.cotto.bitbook.backend.transaction.sochain;

import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.TRANSACTION_HASH_4;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;

public class SoChainAddressTransactionsFixtures {
    public static final SoChainAddressTransactionsDto SOCHAIN_ADDRESS_DETAILS;
    public static final SoChainAddressTransactionsDto SOCHAIN_ADDRESS_UPDATED;

    static {
        SOCHAIN_ADDRESS_DETAILS = new SoChainAddressTransactionsDto(
                ADDRESS,
                Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2)
        );
        SOCHAIN_ADDRESS_UPDATED = new SoChainAddressTransactionsDto(
                ADDRESS,
                Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2, TRANSACTION_HASH_3, TRANSACTION_HASH_4)
        );
    }
}
