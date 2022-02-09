package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.AddressTransactions;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS_UPDATED;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_4;
import static org.assertj.core.api.Assertions.assertThat;

class SimpleAddressTransactionsProviderTest {

    private final TestableAddressTransactionsProvider addressTransactionsProvider =
            new TestableAddressTransactionsProvider();

    @Test
    void getName() {
        assertThat(addressTransactionsProvider.getName()).isEqualTo("abc");
    }

    @Test
    void get_for_unknown_transactions() {
        addressTransactionsProvider.apiResponse = ADDRESS_TRANSACTIONS;
        Optional<AddressTransactions> addressTransactions =
                addressTransactionsProvider.get(new TransactionsRequestKey(AddressTransactions.UNKNOWN, 123));
        assertThat(addressTransactions).contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void updating_known_transactions() {
        addressTransactionsProvider.apiResponse = new AddressTransactions(
                ADDRESS,
                Set.of(TRANSACTION_HASH_3, TRANSACTION_HASH_4),
                LAST_CHECKED_AT_BLOCK_HEIGHT + 50
        );
        TransactionsRequestKey request = new TransactionsRequestKey(ADDRESS_TRANSACTIONS, LAST_CHECKED_AT_BLOCK_HEIGHT);
        Optional<AddressTransactions> addressTransactions = addressTransactionsProvider.get(request);
        assertThat(addressTransactions).contains(ADDRESS_TRANSACTIONS_UPDATED);
    }

    @Test
    void updating_with_unknown() {
        addressTransactionsProvider.apiResponse = AddressTransactions.UNKNOWN;
        TransactionsRequestKey request = new TransactionsRequestKey(ADDRESS_TRANSACTIONS, LAST_CHECKED_AT_BLOCK_HEIGHT);
        Optional<AddressTransactions> addressTransactions = addressTransactionsProvider.get(request);
        assertThat(addressTransactions).contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void no_response_from_api_when_updating() {
        TransactionsRequestKey request = new TransactionsRequestKey(ADDRESS_TRANSACTIONS, LAST_CHECKED_AT_BLOCK_HEIGHT);
        Optional<AddressTransactions> addressTransactions = addressTransactionsProvider.get(request);
        assertThat(addressTransactions).contains(ADDRESS_TRANSACTIONS);
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