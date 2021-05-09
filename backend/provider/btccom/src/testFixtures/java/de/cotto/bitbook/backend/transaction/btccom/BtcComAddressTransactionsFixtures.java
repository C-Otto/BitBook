package de.cotto.bitbook.backend.transaction.btccom;

import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.TRANSACTION_HASH_4;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;

public class BtcComAddressTransactionsFixtures {
    public static final BtcComAddressTransactionsDto BTCCOM_ADDRESS_DETAILS;
    public static final BtcComAddressTransactionsDto BTCCOM_ADDRESS_UPDATED;

    static {
        BTCCOM_ADDRESS_DETAILS = new BtcComAddressTransactionsDto(
                Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2)
        );
        BTCCOM_ADDRESS_UPDATED = new BtcComAddressTransactionsDto(
                Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2, TRANSACTION_HASH_3, TRANSACTION_HASH_4)
        );
    }
}
