package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.ProviderException;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.request.ResultFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.transaction.TransactionsRequestKeyFixtures.ADDRESS_TRANSACTIONS_REQUEST;
import static de.cotto.bitbook.backend.transaction.TransactionsRequestKeyFixtures.TRANSACTIONS_REQUEST_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrioritizingAddressTransactionsProviderTest {
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    private AddressTransactionsProvider addressTransactionsProvider;
    private PrioritizingAddressTransactionsProvider prioritizingPriceProvider;

    @BeforeEach
    void setUp() {
        addressTransactionsProvider = mock(AddressTransactionsProvider.class);
        lenient().when(addressTransactionsProvider.isSupported(any())).thenReturn(true);
        prioritizingPriceProvider = new PrioritizingAddressTransactionsProvider(List.of(addressTransactionsProvider));
    }

    @Test
    void getAddressTransactions() throws Exception {
        when(addressTransactionsProvider.get(TRANSACTIONS_REQUEST_KEY)).thenReturn(Optional.of(ADDRESS_TRANSACTIONS));

        ResultFuture<AddressTransactions> resultFuture =
                prioritizingPriceProvider.getAddressTransactions(ADDRESS_TRANSACTIONS_REQUEST);
        workOnRequestsInBackground();

        assertThat(resultFuture.getResult()).contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void getAddressTransactions_failure() throws Exception {
        when(addressTransactionsProvider.get(TRANSACTIONS_REQUEST_KEY)).thenReturn(Optional.empty());

        ResultFuture<AddressTransactions> resultFuture =
                prioritizingPriceProvider.getAddressTransactions(ADDRESS_TRANSACTIONS_REQUEST);
        workOnRequestsInBackground();

        assertThat(resultFuture.getResult()).isEmpty();
    }

    @Test
    void getAddressTransactions_error() throws Exception {
        when(addressTransactionsProvider.get(TRANSACTIONS_REQUEST_KEY)).thenThrow(ProviderException.class);

        ResultFuture<AddressTransactions> resultFuture =
                prioritizingPriceProvider.getAddressTransactions(ADDRESS_TRANSACTIONS_REQUEST);
        workOnRequestsInBackground();

        assertThat(resultFuture.getResult()).isEmpty();
    }

    @Test
    void getProvidedResultName() {
        assertThat(prioritizingPriceProvider.getProvidedResultName()).isEqualTo("Transactions for address");
    }

    private void workOnRequestsInBackground() {
        executor.execute(() -> prioritizingPriceProvider.workOnRequests());
    }
}