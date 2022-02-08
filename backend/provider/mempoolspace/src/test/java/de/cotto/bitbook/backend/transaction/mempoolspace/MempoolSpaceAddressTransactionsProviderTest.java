package de.cotto.bitbook.backend.transaction.mempoolspace;

import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.transaction.TransactionsRequestKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS_UPDATED;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.mempoolspace.MempoolSpaceAddressTransactionsFixtures.MEMPOOLSPACE_ADDRESS_DETAILS;
import static de.cotto.bitbook.backend.transaction.mempoolspace.MempoolSpaceAddressTransactionsFixtures.MEMPOOLSPACE_ADDRESS_UPDATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SuppressWarnings("CPD-START")
@ExtendWith(MockitoExtension.class)
class MempoolSpaceAddressTransactionsProviderTest {

    @InjectMocks
    private MempoolSpaceAddressTransactionsProvider addressTransactionsProvider;

    @Mock
    private MempoolSpaceClient mempoolSpaceClient;

    @Test
    void getAddressDetails() {
        when(mempoolSpaceClient.getAddressDetails(ADDRESS)).thenReturn(Optional.of(MEMPOOLSPACE_ADDRESS_DETAILS));
        Optional<AddressTransactions> addressTransactions = addressTransactionsProvider.get(
                new TransactionsRequestKey(ADDRESS, LAST_CHECKED_AT_BLOCK_HEIGHT)
        );
        assertThat(addressTransactions).contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void getUpdates() {
        when(mempoolSpaceClient.getAddressDetails(ADDRESS)).thenReturn(Optional.of(MEMPOOLSPACE_ADDRESS_UPDATED));
        TransactionsRequestKey transactionsRequestKey = new TransactionsRequestKey(
                ADDRESS_TRANSACTIONS,
                ADDRESS_TRANSACTIONS_UPDATED.getLastCheckedAtBlockHeight()
        );
        Optional<AddressTransactions> updated =
                addressTransactionsProvider.get(transactionsRequestKey);
        assertThat(updated).contains(ADDRESS_TRANSACTIONS_UPDATED);
    }

    @Test
    void getUpdates_no_update_returned() {
        when(mempoolSpaceClient.getAddressDetails(ADDRESS)).thenReturn(Optional.empty());

        Optional<AddressTransactions> updated = addressTransactionsProvider.get(
                new TransactionsRequestKey(ADDRESS_TRANSACTIONS, LAST_CHECKED_AT_BLOCK_HEIGHT)
        );

        assertThat(updated).contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void getName() {
        assertThat(addressTransactionsProvider.getName())
                .isEqualTo("MempoolSpaceAddressTransactionsProvider");
    }
}