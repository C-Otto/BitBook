package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.request.PrioritizedRequestWithResult;
import de.cotto.bitbook.backend.request.RequestPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS_2;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS_2;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS_BCH;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS_UPDATED;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressTransactionsServiceTest {
    private static final AddressTransactions UNKNOWN_BTC = AddressTransactions.unknown(BTC);
    private static final AddressTransactions UNKNOWN_BCH = AddressTransactions.unknown(BCH);

    @InjectMocks
    private AddressTransactionsService addressTransactionsService;

    @Mock
    private PrioritizingAddressTransactionsProvider addressTransactionsProvider;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AddressTransactionsDao addressTransactionsDao;

    @Mock
    private BlockHeightService blockHeightService;

    @Mock
    private TransactionUpdateHeuristics transactionUpdateHeuristics;

    @BeforeEach
    void setUp() {
        lenient().when(transactionUpdateHeuristics.getRequestWithTweakedPriority(any())).then(returnsFirstArg());
    }

    @Test
    void getTransactionsForAddresses_btc() {
        when(blockHeightService.getBlockHeight(BTC)).thenReturn(LAST_CHECKED_AT_BLOCK_HEIGHT);
        when(addressTransactionsDao.getAddressTransactions(any(), eq(BTC))).thenReturn(UNKNOWN_BTC);
        TransactionsRequestKey requestKey = new TransactionsRequestKey(ADDRESS, BTC, LAST_CHECKED_AT_BLOCK_HEIGHT);
        mockAddressTransactionsFromProvider(requestKey, STANDARD, ADDRESS_TRANSACTIONS);
        TransactionsRequestKey requestKey2 = new TransactionsRequestKey(ADDRESS_2, BTC, LAST_CHECKED_AT_BLOCK_HEIGHT);
        mockAddressTransactionsFromProvider(requestKey2, STANDARD, ADDRESS_TRANSACTIONS_2);

        assertThat(addressTransactionsService.getTransactionsForAddresses(Set.of(ADDRESS, ADDRESS_2), BTC))
                .containsExactlyInAnyOrder(ADDRESS_TRANSACTIONS, ADDRESS_TRANSACTIONS_2);
    }

    @Test
    void getTransactionsForAddresses_bch_only() {
        when(blockHeightService.getBlockHeight(BCH)).thenReturn(LAST_CHECKED_AT_BLOCK_HEIGHT);
        when(addressTransactionsDao.getAddressTransactions(any(), eq(BCH))).thenReturn(UNKNOWN_BCH);
        TransactionsRequestKey requestKey = new TransactionsRequestKey(ADDRESS, BCH, LAST_CHECKED_AT_BLOCK_HEIGHT);
        mockAddressTransactionsFromProvider(requestKey, STANDARD, ADDRESS_TRANSACTIONS_BCH);

        assertThat(addressTransactionsService.getTransactionsForAddresses(Set.of(ADDRESS), BCH))
                .containsExactly(ADDRESS_TRANSACTIONS_BCH);
    }

    private void mockAddressTransactionsFromProvider(
            TransactionsRequestKey requestKey,
            RequestPriority priority,
            AddressTransactions delayedResult
    ) {
        when(addressTransactionsProvider.getAddressTransactions(argIsRequest(requestKey, priority)))
                .then(invocation -> {
                    AddressTransactionsRequest request = invocation.getArgument(0);
                    PrioritizedRequestWithResult<TransactionsRequestKey, AddressTransactions> resultFuture =
                            request.getWithResultFuture();
                    resultFuture.provideResult(delayedResult);
                    return resultFuture;
                });
    }

    private AddressTransactionsRequest argIsRequest(TransactionsRequestKey requestKey, RequestPriority priority) {
        return argThat(request -> request != null
                                  && request.getKey().equals(requestKey)
                                  && request.getPriority().equals(priority)
        );
    }

    @Nested
    class NotKnownInPersistence {
        private final TransactionsRequestKey requestKey =
                new TransactionsRequestKey(ADDRESS, BTC, LAST_CHECKED_AT_BLOCK_HEIGHT);

        @BeforeEach
        void setUp() {
            when(addressTransactionsDao.getAddressTransactions(ADDRESS, BTC)).thenReturn(UNKNOWN_BTC);
        }

        @Test
        void downloads_transactions_for_address() {
            when(blockHeightService.getBlockHeight(BTC)).thenReturn(LAST_CHECKED_AT_BLOCK_HEIGHT);
            mockAddressTransactionsFromProvider(requestKey, STANDARD, ADDRESS_TRANSACTIONS);
            AddressTransactions addressTransactions = addressTransactionsService.getTransactions(ADDRESS, BTC);
            assertThat(addressTransactions).isEqualTo(ADDRESS_TRANSACTIONS);
        }

        @Test
        void requests_transactions() {
            when(blockHeightService.getBlockHeight(BTC)).thenReturn(LAST_CHECKED_AT_BLOCK_HEIGHT);
            mockAddressTransactionsFromProvider(requestKey, STANDARD, ADDRESS_TRANSACTIONS);
            AddressTransactions addressTransactions = addressTransactionsService.getTransactions(ADDRESS, BTC);
            verify(transactionService).requestInBackground(addressTransactions.getTransactionHashes(), BTC);
        }

        @Test
        void requestTransactionsInBackground_requests_transaction_details_in_background() {
            when(blockHeightService.getBlockHeight(BTC)).thenReturn(LAST_CHECKED_AT_BLOCK_HEIGHT);
            mockAddressTransactionsFromProvider(requestKey, LOWEST, ADDRESS_TRANSACTIONS);
            addressTransactionsService.requestTransactionsInBackground(ADDRESS, BTC);
            verify(transactionService).requestInBackground(ADDRESS_TRANSACTIONS.getTransactionHashes(), BTC);
        }

        @Test
        void does_not_persist_unknown_transaction_addresses() {
            when(blockHeightService.getBlockHeight(BTC)).thenReturn(LAST_CHECKED_AT_BLOCK_HEIGHT);
            mockAddressTransactionsFromProvider(requestKey, STANDARD, UNKNOWN_BTC);
            addressTransactionsService.getTransactions(ADDRESS, BTC);
            verify(addressTransactionsDao, never()).saveAddressTransactions(any());
        }

        @Test
        void persists_transaction_addresses() {
            when(blockHeightService.getBlockHeight(BTC)).thenReturn(LAST_CHECKED_AT_BLOCK_HEIGHT);
            mockAddressTransactionsFromProvider(requestKey, STANDARD, ADDRESS_TRANSACTIONS);
            addressTransactionsService.getTransactions(ADDRESS, BTC);
            verify(addressTransactionsDao).saveAddressTransactions(ADDRESS_TRANSACTIONS);
        }
    }

    @Nested
    class AlreadyKnownInPersistence {

        private int updateBlockHeight;
        private TransactionsRequestKey requestKey;

        @BeforeEach
        void setUp() {
            when(addressTransactionsDao.getAddressTransactions(ADDRESS, BTC)).thenReturn(ADDRESS_TRANSACTIONS);
            updateBlockHeight = ADDRESS_TRANSACTIONS_UPDATED.getLastCheckedAtBlockHeight();
            requestKey = new TransactionsRequestKey(ADDRESS_TRANSACTIONS, updateBlockHeight);
            when(blockHeightService.getBlockHeight(BTC)).thenReturn(updateBlockHeight);
        }

        @Test
        void downloads_transactions() {
            when(transactionUpdateHeuristics.isRecentEnough(any())).thenReturn(false);
            mockAddressTransactionsFromProvider(requestKey, STANDARD, ADDRESS_TRANSACTIONS_UPDATED);
            addressTransactionsService.getTransactions(ADDRESS, BTC);
            verify(addressTransactionsProvider).getAddressTransactions(argIsRequest(requestKey, STANDARD));
        }

        @Test
        void downloads_transactions_with_tweaked_priority() {
            when(transactionUpdateHeuristics.isRecentEnough(any())).thenReturn(false);
            AddressTransactionsRequest originalRequest = AddressTransactionsRequest.create(requestKey, STANDARD);
            when(transactionUpdateHeuristics.getRequestWithTweakedPriority(originalRequest))
                    .thenReturn(AddressTransactionsRequest.create(requestKey, LOWEST));
            mockAddressTransactionsFromProvider(requestKey, LOWEST, ADDRESS_TRANSACTIONS_UPDATED);
            addressTransactionsService.getTransactions(ADDRESS, BTC);
            verify(addressTransactionsProvider).getAddressTransactions(argIsRequest(requestKey, LOWEST));
        }

        @Test
        void does_not_update_if_recently_updated() {
            when(transactionUpdateHeuristics.isRecentEnough(any())).thenReturn(true);
            addressTransactionsService.getTransactions(ADDRESS, BTC);
            verifyNoInteractions(addressTransactionsProvider);
        }

        @Test
        void returns_updated_transaction_addresses() {
            mockAddressTransactionsFromProvider(requestKey, STANDARD, ADDRESS_TRANSACTIONS_UPDATED);
            AddressTransactions addressTransactions = addressTransactionsService.getTransactions(ADDRESS, BTC);
            assertThat(addressTransactions).isEqualTo(ADDRESS_TRANSACTIONS_UPDATED);
        }

        @Test
        void returns_known_transaction_addresses_if_update_fails() {
            when(addressTransactionsProvider.getAddressTransactions(argIsRequest(requestKey, STANDARD)))
                    .then(invocation -> {
                        AddressTransactionsRequest request = invocation.getArgument(0);
                        PrioritizedRequestWithResult<TransactionsRequestKey, AddressTransactions> resultFuture =
                                request.getWithResultFuture();
                        resultFuture.stopWithoutResult();
                        return resultFuture;
                    });
            AddressTransactions addressTransactions = addressTransactionsService.getTransactions(ADDRESS, BTC);
            assertThat(addressTransactions).isEqualTo(ADDRESS_TRANSACTIONS);
        }

        @Test
        @SuppressWarnings("FutureReturnValueIgnored")
        void returns_known_transaction_without_delay() {
            ExecutorService executor = Executors.newCachedThreadPool();
            Duration timeout = Duration.ofSeconds(2);
            AtomicBoolean secondRequestReceived = new AtomicBoolean(false);
            when(addressTransactionsDao.getAddressTransactions(ADDRESS_2, BTC)).thenReturn(ADDRESS_TRANSACTIONS);
            when(addressTransactionsProvider.getAddressTransactions(argIsRequest(requestKey, STANDARD)))
                    .then(invocation -> {
                        AddressTransactionsRequest request = invocation.getArgument(0);
                        PrioritizedRequestWithResult<TransactionsRequestKey, AddressTransactions> resultFuture =
                                request.getWithResultFuture();
                        executor.submit(() -> {
                            await().atMost(timeout.plusSeconds(1)).untilTrue(secondRequestReceived);
                            resultFuture.provideResult(ADDRESS_TRANSACTIONS_UPDATED);
                        });
                        return resultFuture;
                    })
                    .then(invocation -> {
                        AddressTransactionsRequest request1 = invocation.getArgument(0);
                        PrioritizedRequestWithResult<TransactionsRequestKey, AddressTransactions> resultFuture1 =
                                request1.getWithResultFuture();
                        resultFuture1.stopWithoutResult();
                        secondRequestReceived.set(true);
                        return resultFuture1;
                    });

            await().atMost(timeout).untilAsserted(
                    () -> {
                        addressTransactionsService.getTransactionsForAddresses(Set.of(ADDRESS, ADDRESS_2), BTC);
                        verify(addressTransactionsProvider, times(2)).getAddressTransactions(any());
                    }
            );
        }

        @Test
        void persists_transaction_addresses() {
            mockAddressTransactionsFromProvider(
                    requestKey, STANDARD, ADDRESS_TRANSACTIONS_UPDATED
            );
            AddressTransactions addressTransactions = addressTransactionsService.getTransactions(ADDRESS, BTC);
            verify(addressTransactionsDao).saveAddressTransactions(addressTransactions);
        }

        @Test
        void persists_current_block_height() {
            mockAddressTransactionsFromProvider(requestKey, STANDARD, ADDRESS_TRANSACTIONS_UPDATED);
            addressTransactionsService.getTransactions(ADDRESS, BTC);
            verify(addressTransactionsDao).saveAddressTransactions(
                    argThat(transactions -> transactions.getLastCheckedAtBlockHeight() == updateBlockHeight)
            );
        }

        @Test
        void does_not_persist_unknown_transaction_addresses() {
            mockAddressTransactionsFromProvider(requestKey, STANDARD, UNKNOWN_BTC);
            addressTransactionsService.getTransactions(ADDRESS, BTC);
            verify(addressTransactionsDao, never()).saveAddressTransactions(any());
        }

        @Test
        void requests_transactions() {
            mockAddressTransactionsFromProvider(
                    requestKey, STANDARD, ADDRESS_TRANSACTIONS_UPDATED
            );
            AddressTransactions addressTransactions = addressTransactionsService.getTransactions(ADDRESS, BTC);
            verify(transactionService).requestInBackground(addressTransactions.getTransactionHashes(), BTC);
        }

        @Test
        void requestTransactionsInBackground_requests_transaction_details_in_background() {
            mockAddressTransactionsFromProvider(requestKey, LOWEST, ADDRESS_TRANSACTIONS_UPDATED);
            addressTransactionsService.requestTransactionsInBackground(ADDRESS, BTC);
            verify(transactionService).requestInBackground(ADDRESS_TRANSACTIONS_UPDATED.getTransactionHashes(), BTC);
        }

        @Test
        void requestTransactionInBackground() {
            mockAddressTransactionsFromProvider(requestKey, LOWEST, ADDRESS_TRANSACTIONS_UPDATED);
            addressTransactionsService.requestTransactionsInBackground(ADDRESS, BTC);
            verify(addressTransactionsProvider).getAddressTransactions(
                    argThat(request -> request.getPriority().equals(LOWEST))
            );
        }
    }

}