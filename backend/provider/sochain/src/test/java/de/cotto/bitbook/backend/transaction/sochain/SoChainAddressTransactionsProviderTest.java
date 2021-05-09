package de.cotto.bitbook.backend.transaction.sochain;

import de.cotto.bitbook.backend.transaction.TransactionsRequestKey;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS_UPDATED;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.sochain.SoChainAddressTransactionsFixtures.SOCHAIN_ADDRESS_DETAILS;
import static de.cotto.bitbook.backend.transaction.sochain.SoChainAddressTransactionsFixtures.SOCHAIN_ADDRESS_UPDATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SoChainAddressTransactionsProviderTest {

    @InjectMocks
    private SoChainAddressTransactionsProvider soChainAddressTransactionsProvider;

    @Mock
    private SoChainClient soChainClient;

    @Test
    void getAddressDetails() {
        when(soChainClient.getAddressDetails(ADDRESS)).thenReturn(Optional.of(SOCHAIN_ADDRESS_DETAILS));
        Optional<AddressTransactions> addressTransactions = soChainAddressTransactionsProvider.get(
                new TransactionsRequestKey(ADDRESS, LAST_CHECKED_AT_BLOCK_HEIGHT)
        );
        assertThat(addressTransactions).contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void getUpdates() {
        when(soChainClient.getAddressDetails(ADDRESS)).thenReturn(Optional.of(SOCHAIN_ADDRESS_UPDATED));
        TransactionsRequestKey transactionsRequestKey = new TransactionsRequestKey(
                ADDRESS_TRANSACTIONS,
                ADDRESS_TRANSACTIONS_UPDATED.getLastCheckedAtBlockHeight()
        );
        Optional<AddressTransactions> updated =
                soChainAddressTransactionsProvider.get(transactionsRequestKey);
        assertThat(updated).contains(ADDRESS_TRANSACTIONS_UPDATED);
    }

    @Test
    void getUpdates_no_update_returned() {
        when(soChainClient.getAddressDetails(ADDRESS)).thenReturn(Optional.empty());

        Optional<AddressTransactions> updated = soChainAddressTransactionsProvider.get(
                new TransactionsRequestKey(ADDRESS_TRANSACTIONS, LAST_CHECKED_AT_BLOCK_HEIGHT)
        );

        assertThat(updated).contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void getName() {
        assertThat(soChainAddressTransactionsProvider.getName()).isEqualTo("SoChainAddressTransactionsProvider");
    }
}