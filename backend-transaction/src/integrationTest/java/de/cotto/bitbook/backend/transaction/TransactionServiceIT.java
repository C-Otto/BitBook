package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TransactionServiceIT {
    private static final int BLOCK_HEIGHT = 700_000;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionDao transactionDao;

    @MockBean
    private PrioritizingTransactionProvider transactionProvider;

    @MockBean
    private PrioritizingBlockHeightProvider blockHeightProvider;

    @BeforeEach
    void setUp() {
        when(transactionProvider.getTransaction(any())).thenReturn(Transaction.UNKNOWN);
    }

    @Test
    void getTransactionDetails() {
        when(transactionProvider.getTransaction(any())).thenReturn(TRANSACTION);
        when(blockHeightProvider.getBlockHeight()).thenReturn(BLOCK_HEIGHT);

        Transaction transaction = transactionService.getTransactionDetails(TRANSACTION_HASH);

        assertThat(transaction).isEqualTo(TRANSACTION);
    }

    @Test
    void waits_for_already_running_request() {
        when(blockHeightProvider.getBlockHeight()).thenReturn(BLOCK_HEIGHT);
        when(transactionProvider.getTransaction(forHash(TRANSACTION_HASH_2))).thenAnswer(
                (Answer<Transaction>) invocation -> {
                    Thread.sleep(200);
                    TransactionRequest request = invocation.getArgument(0);
                    request.getWithResultFuture().provideResult(TRANSACTION_2);
                    return TRANSACTION_2;
                }
        ).thenAnswer(
                (Answer<Transaction>) invocation -> {
                    TransactionRequest request = invocation.getArgument(0);
                    request.getWithResultFuture().provideResult(Transaction.UNKNOWN);
                    return Transaction.UNKNOWN;
                }
        );

        transactionService.getTransactionDetails(TRANSACTION_HASH_2);
        transactionService.requestInBackground(Set.of(TRANSACTION_HASH_2));
        verify(transactionProvider, timeout(50)).getTransaction(forHash(TRANSACTION_HASH_2));
    }

    @Test
    void requestInBackground_async() {
        when(transactionProvider.getTransaction(any())).then((Answer<Transaction>) invocation -> {
            Thread.sleep(1_000);
            return Transaction.UNKNOWN;
        });
        await().atMost(900, MILLISECONDS).untilAsserted(
                () -> transactionService.requestInBackground(Set.of(TRANSACTION_HASH))
        );
    }

    @Test
    void async_request_results_are_persisted() {
        when(blockHeightProvider.getBlockHeight()).thenReturn(BLOCK_HEIGHT);
        mockResult();

        transactionService.requestInBackground(Set.of(TRANSACTION_HASH));

        await().atMost(1, SECONDS).untilAsserted(
                () -> assertThat(transactionDao.getTransaction(TRANSACTION_HASH)).isEqualTo(TRANSACTION)
        );
    }

    @Test
    void many_pending_requests() {
        when(transactionProvider.getTransaction(any())).then(invocation -> {
            Thread.sleep(10);
            return Transaction.UNKNOWN;
        });
        int max = 500;
        Set<String> hashes = IntStream.range(0, max).mapToObj(String::valueOf).collect(Collectors.toSet());
        AtomicReference<Set<Transaction>> results = new AtomicReference<>();
        await().atMost(4, SECONDS).until(() -> {
            results.set(transactionService.getTransactionDetails(hashes));
            return true;
        });
        assertThat(results.get()).isNotEmpty();
    }

    private void mockResult() {
        when(transactionProvider.getTransaction(any())).then((Answer<Transaction>) invocation -> {
            TransactionRequest request = invocation.getArgument(0);
            request.getWithResultFuture().provideResult(TRANSACTION);
            return TRANSACTION;
        });
    }

    @SuppressWarnings("SameParameterValue")
    private TransactionRequest forHash(String transactionHash) {
        return argThat(request -> request != null && transactionHash.equals(request.getHash()));
    }
}
