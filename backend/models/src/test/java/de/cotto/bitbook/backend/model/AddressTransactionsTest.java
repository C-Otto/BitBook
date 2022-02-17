package de.cotto.bitbook.backend.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS_BCH;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS_UPDATED;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_2;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_4;
import static java.util.Collections.emptySet;
import static nl.jqno.equalsverifier.Warning.NULL_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AddressTransactionsTest {
    @Test
    void unknown() {
        assertThat(AddressTransactions.unknown(BTC))
                .isEqualTo(new AddressTransactions(Address.NONE, emptySet(), 0, BTC));
    }

    @Test
    void unknown_different_chain() {
        assertThat(AddressTransactions.unknown(BCH)).isNotEqualTo(AddressTransactions.unknown(BTC));
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
                ", chain='" + BTC + "'" +
                "}");
    }

    @Test
    void testToString_many_transactions() {
        AddressTransactions addressTransactions =
                new AddressTransactions(ADDRESS, manyHashes(10), LAST_CHECKED_AT_BLOCK_HEIGHT, BTC);
        assertThat(addressTransactions).hasToString(
                "AddressTransactions{" +
                "address='" + ADDRESS + "'" +
                ", transactionHashes='" + addressTransactions.getTransactionHashes() + "'" +
                ", lastCheckedAtBlockHeight='678123'" +
                ", chain='" + BTC + "'" +
                "}");
    }

    @Test
    void testToString_too_many_transactions() {
        AddressTransactions addressTransactions =
                new AddressTransactions(ADDRESS, manyHashes(11), LAST_CHECKED_AT_BLOCK_HEIGHT, BTC);
        assertThat(addressTransactions).hasToString(
                "AddressTransactions{" +
                "address='" + ADDRESS + "'" +
                ", transactionHashes='(11 transactions)'" +
                ", lastCheckedAtBlockHeight='678123'" +
                ", chain='" + BTC + "'" +
                "}");
    }

    @Test
    void getTransactionHashes() {
        assertThat(ADDRESS_TRANSACTIONS.getTransactionHashes()).contains(TRANSACTION_HASH, TRANSACTION_HASH_2);
    }

    @Test
    void getChain() {
        assertThat(ADDRESS_TRANSACTIONS.getChain()).isEqualTo(BTC);
    }

    @Test
    void transactionHashes_are_unmodifiable() {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() ->
                ADDRESS_TRANSACTIONS.getTransactionHashes().clear()
        );
    }

    @Test
    void transactionHashes_is_copy_of_original_set() {
        Set<TransactionHash> transactionHashes = new LinkedHashSet<>();
        transactionHashes.add(TRANSACTION_HASH);
        AddressTransactions addressTransactions = new AddressTransactions(ADDRESS, transactionHashes, 456, BTC);
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
        assertThat(AddressTransactions.unknown(BTC).isValid()).isFalse();
    }

    @Test
    void getCombined() {
        Set<TransactionHash> transactionHashes = Set.of(TRANSACTION_HASH_3, TRANSACTION_HASH_4);
        AddressTransactions update = new AddressTransactions(
                ADDRESS,
                transactionHashes,
                ADDRESS_TRANSACTIONS_UPDATED.getLastCheckedAtBlockHeight(),
                BTC
        );
        assertThat(ADDRESS_TRANSACTIONS.getCombined(update)).isEqualTo(ADDRESS_TRANSACTIONS_UPDATED);
    }

    @Test
    void getCombined_argument_without_transactions() {
        AddressTransactions update = new AddressTransactions(ADDRESS, Set.of(), 0, BTC);
        assertThat(ADDRESS_TRANSACTIONS.getCombined(update)).isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void getCombined_different_chains() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> ADDRESS_TRANSACTIONS.getCombined(ADDRESS_TRANSACTIONS_BCH)
        );
    }

    private Set<TransactionHash> manyHashes(int howMany) {
        Set<TransactionHash> result = new LinkedHashSet<>();
        for (int i = 0; i < howMany; i++) {
            result.add(new TransactionHash(String.valueOf(i)));
        }
        return result;
    }
}