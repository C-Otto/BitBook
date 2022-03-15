package de.cotto.bitbook.backend.transaction;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.TransactionsRequestKeyFixtures.TRANSACTIONS_REQUEST_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TransactionsRequestKeyTest {

    private static final TransactionsRequestKey KEY_WITH_TRANSACTIONS =
            new TransactionsRequestKey(ADDRESS_TRANSACTIONS, BLOCK_HEIGHT);

    @Test
    void testEquals() {
        EqualsVerifier.forClass(TransactionsRequestKey.class).usingGetClass().verify();
    }

    @Test
    void without_known_address_transactions() {
        assertThat(TRANSACTIONS_REQUEST_KEY.address()).isEqualTo(ADDRESS);
        assertThat(TRANSACTIONS_REQUEST_KEY.blockHeight()).isEqualTo(BLOCK_HEIGHT);
    }

    @Test
    void without_known_address_transactions_does_not_allow_access_to_transactions() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                TRANSACTIONS_REQUEST_KEY::addressTransactions
        );
    }

    @Test
    void with_known_address_transactions() {
        assertThat(KEY_WITH_TRANSACTIONS.addressTransactions()).isEqualTo(ADDRESS_TRANSACTIONS);
        assertThat(KEY_WITH_TRANSACTIONS.blockHeight()).isEqualTo(BLOCK_HEIGHT);
    }
}