package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.price.PriceService;
import de.cotto.bitbook.backend.request.ResultFuture;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final int BLOCK_COUNT_IN_CHAIN = BLOCK_HEIGHT + 10;

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private PrioritizingTransactionProvider prioritizingTransactionProvider;

    @Mock
    private TransactionDao transactionDao;

    @Mock
    private PriceService priceService;

    @Mock
    private BlockHeightService blockHeightService;

    @BeforeEach
    void setUp() {
        when(transactionDao.getTransaction(any())).thenReturn(Transaction.UNKNOWN);
    }

    @Test
    void downloads_transaction_details_with_standard_priority() {
        mockResult(TRANSACTION_HASH, TRANSACTION);
        when(blockHeightService.getBlockHeight()).thenReturn(BLOCK_COUNT_IN_CHAIN);

        Transaction transaction = transactionService.getTransactionDetails(TRANSACTION_HASH);

        assertThat(transaction).isEqualTo(TRANSACTION);
    }

    @Test
    void downloads_transaction_details_with_standard_priority_by_hashes() {
        mockResult(TRANSACTION_HASH, TRANSACTION);
        mockResult(TRANSACTION_HASH_2, TRANSACTION_2);
        when(blockHeightService.getBlockHeight()).thenReturn(BLOCK_COUNT_IN_CHAIN);

        Set<Transaction> transactions =
                transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2));

        assertThat(transactions).containsExactlyInAnyOrder(TRANSACTION, TRANSACTION_2);
    }

    @Test
    void requests_price_for_transaction_timestamp() {
        mockResult(TRANSACTION_HASH, TRANSACTION);
        when(blockHeightService.getBlockHeight()).thenReturn(BLOCK_COUNT_IN_CHAIN);

        Transaction transaction = transactionService.getTransactionDetails(TRANSACTION_HASH);

        verify(priceService, atLeastOnce()).requestPriceInBackground(transaction.getTime());
    }

    @Test
    void requests_price_for_transaction_timestamp_known_in_persistence() {
        when(transactionDao.getTransaction(TRANSACTION_HASH)).thenReturn(TRANSACTION);

        transactionService.getTransactionDetails(TRANSACTION_HASH);

        verify(priceService).requestPriceInBackground(TRANSACTION.getTime());
    }

    @Test
    void no_price_trigger_for_unknown_transaction() {
        mockResult(TRANSACTION_HASH, Transaction.UNKNOWN);
        transactionService.getTransactionDetails(TRANSACTION_HASH);
        verifyNoMoreInteractions(priceService);
    }

    @Test
    void persists_download_for_confirmed_transaction() {
        mockResult(TRANSACTION_HASH, TRANSACTION);
        when(blockHeightService.getBlockHeight()).thenReturn(BLOCK_COUNT_IN_CHAIN);

        transactionService.getTransactionDetails(TRANSACTION_HASH);

        verify(transactionDao).saveTransaction(TRANSACTION);
    }

    @Test
    void unknown_transaction() {
        mockResult(TRANSACTION_HASH, Transaction.UNKNOWN);

        Transaction transaction = transactionService.getTransactionDetails(TRANSACTION_HASH);

        assertThat(transaction).isEqualTo(Transaction.UNKNOWN);
    }

    @Test
    void does_not_persist_download_for_unknown_transaction() {
        mockResult(TRANSACTION_HASH, Transaction.UNKNOWN);

        transactionService.getTransactionDetails(TRANSACTION_HASH);

        verify(transactionDao, never()).saveTransaction(any());
    }

    @Test
    void does_not_persist_download_for_unconfirmed_transaction() {
        Transaction downloadedTransaction = new Transaction(TRANSACTION_HASH, -1);
        mockResult(TRANSACTION_HASH, downloadedTransaction);

        transactionService.getTransactionDetails(TRANSACTION_HASH);

        verify(transactionDao, never()).saveTransaction(any());
    }

    @Test
    void unconfirmed_transaction_is_treated_as_unknown() {
        Transaction downloadedTransaction = new Transaction(TRANSACTION_HASH, -1);
        mockResult(TRANSACTION_HASH, downloadedTransaction);

        Transaction transaction = transactionService.getTransactionDetails(TRANSACTION_HASH);

        assertThat(transaction).isEqualTo(Transaction.UNKNOWN);
    }

    @Test
    void does_not_persist_download_for_recent_transaction() {
        int confirmationHeight = BLOCK_COUNT_IN_CHAIN - 5;
        Transaction downloadedTransaction = new Transaction(TRANSACTION_HASH, confirmationHeight);
        mockResult(TRANSACTION_HASH, downloadedTransaction);
        transactionService.getTransactionDetails(TRANSACTION_HASH);

        verify(transactionDao, never()).saveTransaction(any());
    }

    @Test
    void recent_transaction_is_returned_as_unknown_transaction() {
        int confirmationHeight = BLOCK_COUNT_IN_CHAIN - 5;
        Transaction downloadedTransaction = new Transaction(TRANSACTION_HASH, confirmationHeight);
        mockResult(TRANSACTION_HASH, downloadedTransaction);

        Transaction transaction = transactionService.getTransactionDetails(TRANSACTION_HASH);

        assertThat(transaction).isEqualTo(Transaction.UNKNOWN);
    }

    @Test
    void uses_persisted_download() {
        when(transactionDao.getTransaction(TRANSACTION_HASH)).thenReturn(TRANSACTION);

        Transaction transaction = transactionService.getTransactionDetails(TRANSACTION_HASH);

        verify(transactionDao, never()).saveTransaction(any());
        assertThat(transaction).isEqualTo(TRANSACTION);
    }

    @Test
    void requests_price_when_transaction_was_persisted() {
        when(transactionDao.getTransaction(TRANSACTION_HASH)).thenReturn(TRANSACTION);
        Transaction transaction = transactionService.getTransactionDetails(TRANSACTION_HASH);
        verify(priceService).requestPriceInBackground(transaction.getTime());
    }

    @Test
    void request_in_background_uses_lowest_priority() {
        String anotherHash = "xxx";
        mockResult(TRANSACTION_HASH, TRANSACTION);
        mockResult(anotherHash, Transaction.UNKNOWN);
        transactionService.requestInBackground(Set.of(TRANSACTION_HASH, anotherHash));
        verify(prioritizingTransactionProvider, times(2))
                .getTransaction(argThat(request -> request.getPriority() == LOWEST));
    }

    private void mockResult(String transactionHash, Transaction transaction) {
        ResultFuture<Transaction> resultFuture = new ResultFuture<>();
        resultFuture.provideResult(transaction);
        when(prioritizingTransactionProvider.getTransaction(requestFor(transactionHash))).thenReturn(resultFuture);
    }

    private TransactionRequest requestFor(String transactionHash) {
        return argThat(request -> request != null && request.getHash().equals(transactionHash));
    }
}