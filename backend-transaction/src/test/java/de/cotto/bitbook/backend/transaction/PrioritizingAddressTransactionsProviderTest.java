package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static de.cotto.bitbook.backend.transaction.TransactionsRequestKeyFixtures.ADDRESS_TRANSACTIONS_REQUEST;
import static de.cotto.bitbook.backend.transaction.TransactionsRequestKeyFixtures.TRANSACTIONS_REQUEST_KEY;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrioritizingAddressTransactionsProviderTest {
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    private AddressTransactionsProvider addressTransactionsProvider;
    private PrioritizingAddressTransactionsProvider prioritizingPriceProvider;

    @BeforeEach
    void setUp() {
        addressTransactionsProvider = mock(AddressTransactionsProvider.class);
        prioritizingPriceProvider = new PrioritizingAddressTransactionsProvider(List.of(addressTransactionsProvider));
    }

    @Test
    void getAddressTransactions() {
        when(addressTransactionsProvider.get(TRANSACTIONS_REQUEST_KEY)).thenReturn(Optional.of(ADDRESS_TRANSACTIONS));
        workOnRequestsInBackground();

        AddressTransactions result =
                prioritizingPriceProvider.getAddressTransactions(ADDRESS_TRANSACTIONS_REQUEST);
        assertThat(result).isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void getAddressTransactions_failure() {
        when(addressTransactionsProvider.get(TRANSACTIONS_REQUEST_KEY)).thenReturn(Optional.empty());
        workOnRequestsInBackground();

        AddressTransactions result =
                prioritizingPriceProvider.getAddressTransactions(ADDRESS_TRANSACTIONS_REQUEST);
        assertThat(result).isEqualTo(AddressTransactions.UNKNOWN);
    }

    @Test
    void getProvidedResultName() {
        assertThat(prioritizingPriceProvider.getProvidedResultName()).isEqualTo("Transactions for address");
    }

    private void workOnRequestsInBackground() {
        executor.execute(() -> {
            await().atMost(1, SECONDS)
                    .until(() -> !prioritizingPriceProvider.getRequestQueue().isEmpty());
            prioritizingPriceProvider.workOnRequests();
        });
    }
}