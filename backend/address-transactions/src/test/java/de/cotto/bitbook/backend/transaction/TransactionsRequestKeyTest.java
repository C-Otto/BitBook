package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.transaction.TransactionsRequestKeyFixtures.TRANSACTIONS_REQUEST_KEY;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
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
        assertThat(TRANSACTIONS_REQUEST_KEY.getAddress()).isEqualTo(ADDRESS);
        assertThat(TRANSACTIONS_REQUEST_KEY.getBlockHeight()).isEqualTo(BLOCK_HEIGHT);
    }

    @Test
    void without_known_address_transactions_does_not_allow_access_to_transactions() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                TRANSACTIONS_REQUEST_KEY::getAddressTransactions
        );
    }

    @Test
    void with_known_address_transactions() {
        assertThat(KEY_WITH_TRANSACTIONS.getAddressTransactions()).isEqualTo(ADDRESS_TRANSACTIONS);
        assertThat(KEY_WITH_TRANSACTIONS.getBlockHeight()).isEqualTo(BLOCK_HEIGHT);
    }

    @Test
    void testToString_without_transactions() {
        assertThat(TRANSACTIONS_REQUEST_KEY).hasToString(
                "TransactionsRequestKey{" +
                "address='" + ADDRESS + "'" +
                ", addressTransactions='" + AddressTransactions.UNKNOWN + "'" +
                ", blockHeight=" + BLOCK_HEIGHT +
                "}"
        );
    }

    @Test
    void testToString_with_transactions() {
        assertThat(KEY_WITH_TRANSACTIONS).hasToString(
                "TransactionsRequestKey{" +
                "address='" + ADDRESS + "'" +
                ", addressTransactions='" + ADDRESS_TRANSACTIONS + "'" +
                ", blockHeight=" + BLOCK_HEIGHT +
                "}"
        );
    }
}