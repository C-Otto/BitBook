package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.request.PrioritizedRequestWithResult;
import de.cotto.bitbook.backend.request.ResultFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class TransactionServiceIT {
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
        ResultFuture<Transaction> resultFuture = new ResultFuture<>();
        resultFuture.stopWithoutResult();
        when(transactionProvider.getTransaction(any())).thenReturn(resultFuture);
    }

    @Test
    void getTransactionDetails() {
        ResultFuture<Transaction> resultFuture = new ResultFuture<>();
        resultFuture.provideResult(TRANSACTION);
        when(transactionProvider.getTransaction(any())).thenReturn(resultFuture);
        when(blockHeightProvider.getBlockHeight(Chain.BTC)).thenReturn(BLOCK_HEIGHT);

        Transaction transaction = transactionService.getTransactionDetails(TRANSACTION_HASH);

        assertThat(transaction).isEqualTo(TRANSACTION);
    }

    @Test
    void async_request_results_are_persisted() {
        when(blockHeightProvider.getBlockHeight(Chain.BTC)).thenReturn(BLOCK_HEIGHT);
        mockResult();

        transactionService.requestInBackground(Set.of(TRANSACTION_HASH));

        await().atMost(1, SECONDS).untilAsserted(
                () -> assertThat(transactionDao.getTransaction(TRANSACTION_HASH)).isEqualTo(TRANSACTION)
        );
    }

    @Test
    @SuppressWarnings("FutureReturnValueIgnored")
    void many_pending_requests() {
        int max = 200;
        ExecutorService executor = Executors.newFixedThreadPool(max);
        when(transactionProvider.getTransaction(any())).then(invocation -> {
            ResultFuture<Transaction> future = new ResultFuture<>();
            executor.submit(() -> abortFutureAfterDelay(future));
            return future;
        });
        Set<TransactionHash> hashes = IntStream.range(0, max).mapToObj(String::valueOf)
                .map(TransactionHash::new)
                .collect(toSet());
        AtomicReference<Set<Transaction>> results = new AtomicReference<>();
        await().atMost(10, SECONDS).until(() -> {
            results.set(transactionService.getTransactionDetails(hashes));
            return true;
        });
        assertThat(results.get()).isNotEmpty();
    }

    private void abortFutureAfterDelay(ResultFuture<Transaction> future) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // ignore
        }
        future.stopWithoutResult();
    }

    private void mockResult() {
        when(transactionProvider.getTransaction(any())).then((Answer<ResultFuture<Transaction>>) invocation -> {
            TransactionRequest request = invocation.getArgument(0);
            PrioritizedRequestWithResult<TransactionHash, Transaction> resultFuture = request.getWithResultFuture();
            resultFuture.provideResult(TRANSACTION);
            return resultFuture;
        });
    }
}
