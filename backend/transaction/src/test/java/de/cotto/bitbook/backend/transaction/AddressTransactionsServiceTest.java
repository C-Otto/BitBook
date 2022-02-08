package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.request.PrioritizedRequestWithResult;
import de.cotto.bitbook.backend.request.RequestPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressTransactions.UNKNOWN;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS_2;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS_UPDATED;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.TransactionFixtures.ADDRESS_2;
import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressTransactionsServiceTest {
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

    @Test
    void getTransactionsForAddresses() {
        when(blockHeightService.getBlockHeight(Chain.BTC)).thenReturn(LAST_CHECKED_AT_BLOCK_HEIGHT);
        when(addressTransactionsDao.getAddressTransactions(any())).thenReturn(UNKNOWN);
        TransactionsRequestKey requestKey = new TransactionsRequestKey(ADDRESS, LAST_CHECKED_AT_BLOCK_HEIGHT);
        mockAddressTransactionsFromProvider(requestKey, STANDARD, ADDRESS_TRANSACTIONS);
        TransactionsRequestKey requestKey2 = new TransactionsRequestKey(ADDRESS_2, LAST_CHECKED_AT_BLOCK_HEIGHT);
        mockAddressTransactionsFromProvider(requestKey2, STANDARD, ADDRESS_TRANSACTIONS_2);

        assertThat(addressTransactionsService.getTransactionsForAddresses(Set.of(ADDRESS, ADDRESS_2)))
                .containsExactlyInAnyOrder(ADDRESS_TRANSACTIONS, ADDRESS_TRANSACTIONS_2);
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
                new TransactionsRequestKey(ADDRESS, LAST_CHECKED_AT_BLOCK_HEIGHT);

        @BeforeEach
        void setUp() {
            when(addressTransactionsDao.getAddressTransactions(ADDRESS)).thenReturn(UNKNOWN);
        }

        @Test
        void downloads_transactions_for_address() {
            when(blockHeightService.getBlockHeight(Chain.BTC)).thenReturn(LAST_CHECKED_AT_BLOCK_HEIGHT);
            mockAddressTransactionsFromProvider(requestKey, STANDARD, ADDRESS_TRANSACTIONS);
            AddressTransactions addressTransactions = addressTransactionsService.getTransactions(ADDRESS);
            assertThat(addressTransactions).isEqualTo(ADDRESS_TRANSACTIONS);
        }

        @Test
        void requests_transactions() {
            when(blockHeightService.getBlockHeight(Chain.BTC)).thenReturn(LAST_CHECKED_AT_BLOCK_HEIGHT);
            mockAddressTransactionsFromProvider(requestKey, STANDARD, ADDRESS_TRANSACTIONS);
            AddressTransactions addressTransactions = addressTransactionsService.getTransactions(ADDRESS);
            verify(transactionService).requestInBackground(addressTransactions.getTransactionHashes());
        }

        @Test
        void requestTransactionsInBackground_requests_transaction_details_in_background() {
            when(blockHeightService.getBlockHeight(Chain.BTC)).thenReturn(LAST_CHECKED_AT_BLOCK_HEIGHT);
            mockAddressTransactionsFromProvider(requestKey, LOWEST, ADDRESS_TRANSACTIONS);
            addressTransactionsService.requestTransactionsInBackground(ADDRESS);
            verify(transactionService).requestInBackground(ADDRESS_TRANSACTIONS.getTransactionHashes());
        }

        @Test
        void does_not_persist_unknown_transaction_addresses() {
            when(blockHeightService.getBlockHeight(Chain.BTC)).thenReturn(LAST_CHECKED_AT_BLOCK_HEIGHT);
            mockAddressTransactionsFromProvider(requestKey, STANDARD, UNKNOWN);
            addressTransactionsService.getTransactions(ADDRESS);
            verify(addressTransactionsDao, never()).saveAddressTransactions(any());
        }

        @Test
        void persists_transaction_addresses() {
            when(blockHeightService.getBlockHeight(Chain.BTC)).thenReturn(LAST_CHECKED_AT_BLOCK_HEIGHT);
            mockAddressTransactionsFromProvider(requestKey, STANDARD, ADDRESS_TRANSACTIONS);
            addressTransactionsService.getTransactions(ADDRESS);
            verify(addressTransactionsDao).saveAddressTransactions(ADDRESS_TRANSACTIONS);
        }
    }

    @Nested
    class AlreadyKnownInPersistence {

        private int updateBlockHeight;
        private TransactionsRequestKey requestKey;

        @BeforeEach
        void setUp() {
            when(addressTransactionsDao.getAddressTransactions(ADDRESS)).thenReturn(ADDRESS_TRANSACTIONS);
            updateBlockHeight = ADDRESS_TRANSACTIONS_UPDATED.getLastCheckedAtBlockHeight();
            requestKey = new TransactionsRequestKey(ADDRESS_TRANSACTIONS, updateBlockHeight);
            when(blockHeightService.getBlockHeight(Chain.BTC)).thenReturn(updateBlockHeight);
        }

        @Test
        void downloads_transactions() {
            when(transactionUpdateHeuristics.isRecentEnough(any())).thenReturn(false);
            mockAddressTransactionsFromProvider(requestKey, STANDARD, ADDRESS_TRANSACTIONS_UPDATED);
            addressTransactionsService.getTransactions(ADDRESS);
            verify(addressTransactionsProvider).getAddressTransactions(argIsRequest(requestKey, STANDARD));
        }

        @Test
        void does_not_update_if_recently_updated() {
            when(transactionUpdateHeuristics.isRecentEnough(any())).thenReturn(true);
            addressTransactionsService.getTransactions(ADDRESS);
            verifyNoInteractions(addressTransactionsProvider);
        }

        @Test
        void returns_updated_transaction_addresses() {
            mockAddressTransactionsFromProvider(requestKey, STANDARD, ADDRESS_TRANSACTIONS_UPDATED);
            AddressTransactions addressTransactions = addressTransactionsService.getTransactions(ADDRESS);
            assertThat(addressTransactions).isEqualTo(ADDRESS_TRANSACTIONS_UPDATED);
        }

        @Test
        void persists_transaction_addresses() {
            mockAddressTransactionsFromProvider(
                    requestKey, STANDARD, ADDRESS_TRANSACTIONS_UPDATED
            );
            AddressTransactions addressTransactions = addressTransactionsService.getTransactions(ADDRESS);
            verify(addressTransactionsDao).saveAddressTransactions(addressTransactions);
        }

        @Test
        void persists_current_block_height() {
            mockAddressTransactionsFromProvider(requestKey, STANDARD, ADDRESS_TRANSACTIONS_UPDATED);
            addressTransactionsService.getTransactions(ADDRESS);
            verify(addressTransactionsDao).saveAddressTransactions(
                    argThat(transactions -> transactions.getLastCheckedAtBlockHeight() == updateBlockHeight)
            );
        }

        @Test
        void does_not_persist_unknown_transaction_addresses() {
            mockAddressTransactionsFromProvider(requestKey, STANDARD, UNKNOWN);
            addressTransactionsService.getTransactions(ADDRESS);
            verify(addressTransactionsDao, never()).saveAddressTransactions(any());
        }

        @Test
        void requests_transactions() {
            mockAddressTransactionsFromProvider(
                    requestKey, STANDARD, ADDRESS_TRANSACTIONS_UPDATED
            );
            AddressTransactions addressTransactions = addressTransactionsService.getTransactions(ADDRESS);
            verify(transactionService).requestInBackground(addressTransactions.getTransactionHashes());
        }

        @Test
        void requestTransactionsInBackground_requests_transaction_details_in_background() {
            mockAddressTransactionsFromProvider(requestKey, LOWEST, ADDRESS_TRANSACTIONS_UPDATED);
            addressTransactionsService.requestTransactionsInBackground(ADDRESS);
            verify(transactionService).requestInBackground(ADDRESS_TRANSACTIONS_UPDATED.getTransactionHashes());
        }

        @Test
        void requestTransactionInBackground() {
            mockAddressTransactionsFromProvider(requestKey, LOWEST, ADDRESS_TRANSACTIONS_UPDATED);
            addressTransactionsService.requestTransactionsInBackground(ADDRESS);
            verify(addressTransactionsProvider).getAddressTransactions(
                    argThat(request -> request.getPriority().equals(LOWEST))
            );
        }
    }

}