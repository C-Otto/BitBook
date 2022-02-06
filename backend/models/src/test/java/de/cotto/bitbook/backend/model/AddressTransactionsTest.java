package de.cotto.bitbook.backend.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressTransactions.UNKNOWN;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS_UPDATED;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.TRANSACTION_HASH_4;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH_2;
import static java.util.Collections.emptySet;
import static nl.jqno.equalsverifier.Warning.NULL_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AddressTransactionsTest {
    @Test
    void unknown() {
        assertThat(UNKNOWN).isEqualTo(new AddressTransactions("", emptySet(), 0));
    }

    @Test
    void testEquals() {
        EqualsVerifier.configure().suppress(NULL_FIELDS).forClass(AddressTransactions.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        assertThat(ADDRESS_TRANSACTIONS).hasToString(
                "AddressTransactions{" +
                "address='" + ADDRESS + "'" +
                ", transactionHashes='" + Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2) + "'" +
                ", lastCheckedAtBlockHeight='678123'" +
                "}");
    }

    @Test
    void testToString_many_transactions() {
        AddressTransactions addressTransactions =
                new AddressTransactions(ADDRESS, manyStrings(10), LAST_CHECKED_AT_BLOCK_HEIGHT);
        assertThat(addressTransactions).hasToString(
                "AddressTransactions{" +
                "address='" + ADDRESS + "'" +
                ", transactionHashes='" + addressTransactions.getTransactionHashes() + "'" +
                ", lastCheckedAtBlockHeight='678123'" +
                "}");
    }

    @Test
    void testToString_too_many_transactions() {
        AddressTransactions addressTransactions =
                new AddressTransactions(ADDRESS, manyStrings(11), LAST_CHECKED_AT_BLOCK_HEIGHT);
        assertThat(addressTransactions).hasToString(
                "AddressTransactions{" +
                "address='" + ADDRESS + "'" +
                ", transactionHashes='(11 transactions)'" +
                ", lastCheckedAtBlockHeight='678123'" +
                "}");
    }

    @Test
    void getTransactionHashes() {
        assertThat(ADDRESS_TRANSACTIONS.getTransactionHashes()).contains(TRANSACTION_HASH, TRANSACTION_HASH_2);
    }

    @Test
    void transactionHashes_are_unmodifiable() {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() ->
                ADDRESS_TRANSACTIONS.getTransactionHashes().clear()
        );
    }

    @Test
    void transactionHashes_is_copy_of_original_set() {
        Set<String> transactionHashes = new LinkedHashSet<>();
        transactionHashes.add(TRANSACTION_HASH);
        AddressTransactions addressTransactions = new AddressTransactions(ADDRESS, transactionHashes, 456);
        transactionHashes.clear();
        assertThat(addressTransactions.getTransactionHashes()).contains(TRANSACTION_HASH);
    }

    @Test
    void getAddress() {
        assertThat(ADDRESS_TRANSACTIONS.getAddress()).isEqualTo(ADDRESS);
    }

    @Test
    void getLastCheckedAtBlockHeight() {
        assertThat(ADDRESS_TRANSACTIONS.getLastCheckedAtBlockHeight()).isEqualTo(LAST_CHECKED_AT_BLOCK_HEIGHT);
    }

    @Test
    void isValid() {
        assertThat(ADDRESS_TRANSACTIONS.isValid()).isTrue();
    }

    @Test
    void unknown_is_not_valid() {
        assertThat(UNKNOWN.isValid()).isFalse();
    }

    @Test
    void getCombined() {
        Set<String> transactionHashes = Set.of(TRANSACTION_HASH_3, TRANSACTION_HASH_4);
        AddressTransactions update = new AddressTransactions(
                ADDRESS,
                transactionHashes,
                ADDRESS_TRANSACTIONS_UPDATED.getLastCheckedAtBlockHeight()
        );
        assertThat(ADDRESS_TRANSACTIONS.getCombined(update)).isEqualTo(ADDRESS_TRANSACTIONS_UPDATED);
    }

    @Test
    void getCombined_argument_without_transactions() {
        AddressTransactions update = new AddressTransactions(ADDRESS, Set.of(), 0);
        assertThat(ADDRESS_TRANSACTIONS.getCombined(update)).isEqualTo(ADDRESS_TRANSACTIONS);
    }

    private Set<String> manyStrings(int howMany) {
        Set<String> result = new LinkedHashSet<>();
        for (int i = 0; i < howMany; i++) {
            result.add(String.valueOf(i));
        }
        return result;
    }
}