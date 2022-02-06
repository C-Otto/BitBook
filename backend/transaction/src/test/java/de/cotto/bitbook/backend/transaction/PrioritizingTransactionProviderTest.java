package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.ProviderException;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.request.ResultFuture;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrioritizingTransactionProviderTest {
    private static final String OTHER_HASH = "xxx";

    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    private PrioritizingTransactionProvider prioritizingTransactionProvider;
    private TransactionProvider transactionProvider1;
    private TransactionProvider transactionProvider2;

    @BeforeEach
    void setUp() {
        transactionProvider1 = mock(TransactionProvider.class);
        transactionProvider2 = mock(TransactionProvider.class);
        lenient().when(transactionProvider1.isSupported(any())).thenReturn(true);
        lenient().when(transactionProvider2.isSupported(any())).thenReturn(true);
        prioritizingTransactionProvider =
                new PrioritizingTransactionProvider(List.of(transactionProvider1, transactionProvider2));
    }

    @Test
    void getTransaction() throws Exception {
        when(transactionProvider1.get(any())).thenReturn(Optional.of(TRANSACTION));
        when(transactionProvider2.get(any())).thenReturn(Optional.of(TRANSACTION));
        assertThat(get()).isEqualTo(TRANSACTION);
    }

    @Test
    void uses_second_provider_if_first_throws_provider_exception() throws Exception {
        when(transactionProvider1.get(any())).thenThrow(ProviderException.class);
        when(transactionProvider2.get(any())).thenReturn(Optional.of(TRANSACTION));
        assertThat(get()).isEqualTo(TRANSACTION);
    }

    @Test
    void uses_second_provider_if_first_is_rate_limited() throws Exception {
        when(transactionProvider1.get(any())).thenThrow(RequestNotPermitted.class);
        when(transactionProvider2.get(any())).thenReturn(Optional.of(TRANSACTION));
        assertThat(get()).isEqualTo(TRANSACTION);
    }

    @Test
    void uses_second_provider_if_first_has_feign_failure() throws Exception {
        when(transactionProvider1.get(any())).thenThrow(FeignException.class);
        when(transactionProvider2.get(any())).thenReturn(Optional.of(TRANSACTION));
        assertThat(get()).isEqualTo(TRANSACTION);
    }

    @Test
    void uses_second_provider_if_first_is_disabled_via_circuit_breaker() throws Exception {
        when(transactionProvider1.get(any())).thenThrow(CallNotPermittedException.class);
        when(transactionProvider2.get(any())).thenReturn(Optional.of(TRANSACTION));
        assertThat(get()).isEqualTo(TRANSACTION);
    }

    @Test
    void gives_unknown_transaction_if_all_providers_fail() throws Exception {
        mockAllFail();
        assertThat(get()).isEqualTo(Transaction.UNKNOWN);
    }

    @Test
    void clears_lowest_priority_requests_if_all_providers_fail() throws Exception {
        mockAllFail();
        prioritizingTransactionProvider.getTransaction(new TransactionRequest(OTHER_HASH, LOWEST));

        get();

        verify(transactionProvider1, never()).get(OTHER_HASH);
        verify(transactionProvider2, never()).get(OTHER_HASH);
    }

    @Test
    void retains_all_but_lowest_priority_requests_if_all_providers_fail() throws Exception {
        mockAllFail();
        prioritizingTransactionProvider.getTransaction(new TransactionRequest(OTHER_HASH, STANDARD));
        prioritizingTransactionProvider.getTransaction(new TransactionRequest(TRANSACTION_HASH, STANDARD));
        workOnRequestsInBackground();

        verify(transactionProvider1, timeout(1_000)).get(TRANSACTION_HASH);
        verify(transactionProvider1, timeout(1_000)).get(OTHER_HASH);
        verify(transactionProvider2, timeout(1_000)).get(TRANSACTION_HASH);
        verify(transactionProvider2, timeout(1_000)).get(OTHER_HASH);
    }

    @Test
    void getProvidedResultName() {
        assertThat(prioritizingTransactionProvider.getProvidedResultName()).isEqualTo("Transaction details");
    }

    private Transaction get() {
        TransactionRequest request = new TransactionRequest(TRANSACTION_HASH, STANDARD);
        ResultFuture<Transaction> resultFuture = prioritizingTransactionProvider.getTransaction(request);
        workOnRequestsInBackground();
        return resultFuture.getResult().orElse(Transaction.UNKNOWN);
    }

    private void workOnRequestsInBackground() {
        executor.execute(() -> prioritizingTransactionProvider.workOnRequests());
    }

    private void mockAllFail() throws Exception {
        when(transactionProvider1.get(any())).thenThrow(CallNotPermittedException.class);
        when(transactionProvider2.get(any())).thenThrow(RequestNotPermitted.class);
    }
}