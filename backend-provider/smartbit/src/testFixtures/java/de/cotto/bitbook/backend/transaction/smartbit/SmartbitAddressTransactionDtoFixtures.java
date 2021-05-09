package de.cotto.bitbook.backend.transaction.smartbit;

import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.TRANSACTION_HASH_4;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;

public class SmartbitAddressTransactionDtoFixtures {
    public static final SmartbitAddressTransactionsDto SMARTBIT_ADDRESS_TRANSACTIONS;
    public static final SmartbitAddressTransactionsDto SMARTBIT_TRANSACTIONS_UPDATED;

    static {
        SMARTBIT_ADDRESS_TRANSACTIONS = new SmartbitAddressTransactionsDto(
                ADDRESS,
                Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2)
        );

        SMARTBIT_TRANSACTIONS_UPDATED = new SmartbitAddressTransactionsDto(
                ADDRESS,
                Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2, TRANSACTION_HASH_3, TRANSACTION_HASH_4)
        );
    }
}
