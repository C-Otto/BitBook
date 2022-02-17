package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.ProviderException;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS_UPDATED;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SimpleAddressTransactionsProviderTest {

    private static final AddressTransactions UNKNOWN_BTC = AddressTransactions.unknown(BTC);
    private final TestableAddressTransactionsProvider addressTransactionsProvider =
            new TestableAddressTransactionsProvider();

    @Test
    void getName() {
        assertThat(addressTransactionsProvider.getName()).isEqualTo("abc");
    }

    @Test
    void get_for_unknown_transactions() throws Exception {
        addressTransactionsProvider.apiResponse = ADDRESS_TRANSACTIONS;
        Optional<AddressTransactions> addressTransactions =
                addressTransactionsProvider.get(new TransactionsRequestKey(UNKNOWN_BTC, 123));
        assertThat(addressTransactions).contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void updating_known_transactions() throws Exception {
        addressTransactionsProvider.apiResponse = new AddressTransactions(
                ADDRESS,
                Set.of(TRANSACTION_HASH_3, TRANSACTION_HASH_4),
                LAST_CHECKED_AT_BLOCK_HEIGHT + 50,
                BTC
        );
        TransactionsRequestKey request = new TransactionsRequestKey(ADDRESS_TRANSACTIONS, LAST_CHECKED_AT_BLOCK_HEIGHT);
        Optional<AddressTransactions> addressTransactions = addressTransactionsProvider.get(request);
        assertThat(addressTransactions).contains(ADDRESS_TRANSACTIONS_UPDATED);
    }

    @Test
    void updating_with_unknown() throws Exception {
        addressTransactionsProvider.apiResponse = UNKNOWN_BTC;
        TransactionsRequestKey request = new TransactionsRequestKey(ADDRESS_TRANSACTIONS, LAST_CHECKED_AT_BLOCK_HEIGHT);
        Optional<AddressTransactions> addressTransactions = addressTransactionsProvider.get(request);
        assertThat(addressTransactions).contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void no_response_from_api_when_updating() throws Exception {
        TransactionsRequestKey request = new TransactionsRequestKey(ADDRESS_TRANSACTIONS, LAST_CHECKED_AT_BLOCK_HEIGHT);
        Optional<AddressTransactions> addressTransactions = addressTransactionsProvider.get(request);
        assertThat(addressTransactions).contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void isSupported_btc() {
        TransactionsRequestKey key = new TransactionsRequestKey(new Address("def"), BTC, 123);
        assertThat(addressTransactionsProvider.isSupported(key)).isTrue();
    }

    @Test
    void isSupported_bch() {
        TransactionsRequestKey key = new TransactionsRequestKey(new Address("abc"), BCH, 123);
        assertThat(addressTransactionsProvider.isSupported(key)).isFalse();
    }

    @Test
    void get_unsupported() {
        TransactionsRequestKey key = new TransactionsRequestKey(new Address("def"), BCH, 123);
        assertThatExceptionOfType(ProviderException.class).isThrownBy(() -> addressTransactionsProvider.get(key));
    }

    private static class TestableAddressTransactionsProvider extends SimpleAddressTransactionsProvider {
        @Nullable
        public AddressTransactions apiResponse;

        @Override
        protected Optional<AddressTransactions> getFromApi(TransactionsRequestKey transactionsRequestKey) {
            return Optional.ofNullable(apiResponse);
        }

        @Override
        public String getName() {
            return "abc";
        }
    }
}