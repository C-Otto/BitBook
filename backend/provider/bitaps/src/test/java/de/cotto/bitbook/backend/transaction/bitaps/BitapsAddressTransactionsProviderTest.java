package de.cotto.bitbook.backend.transaction.bitaps;

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
import static de.cotto.bitbook.backend.transaction.bitaps.BitapsAddressTransactionDtoFixtures.BITAPS_ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.transaction.bitaps.BitapsAddressTransactionDtoFixtures.BITAPS_TRANSACTIONS_UPDATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BitapsAddressTransactionsProviderTest {

    @InjectMocks
    private BitapsAddressTransactionsProvider bitapsAddressTransactionsProvider;

    @Mock
    private BitapsClient bitapsClient;

    @Test
    void getAddressTransactions() {
        when(bitapsClient.getAddressTransactions(ADDRESS)).thenReturn(Optional.of(BITAPS_ADDRESS_TRANSACTIONS));
        Optional<AddressTransactions> addressTransactions = bitapsAddressTransactionsProvider.get(
                new TransactionsRequestKey(ADDRESS, LAST_CHECKED_AT_BLOCK_HEIGHT)
        );
        assertThat(addressTransactions).contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void getUpdates() {
        when(bitapsClient.getAddressTransactions(ADDRESS)).thenReturn(Optional.of(BITAPS_TRANSACTIONS_UPDATED));
        TransactionsRequestKey transactionsRequestKey = new TransactionsRequestKey(
                ADDRESS_TRANSACTIONS,
                ADDRESS_TRANSACTIONS_UPDATED.getLastCheckedAtBlockHeight()
        );
        Optional<AddressTransactions> updated =
                bitapsAddressTransactionsProvider.get(transactionsRequestKey);
        assertThat(updated).contains(ADDRESS_TRANSACTIONS_UPDATED);
    }

    @Test
    void getUpdates_no_update_returned() {
        when(bitapsClient.getAddressTransactions(ADDRESS)).thenReturn(Optional.empty());

        Optional<AddressTransactions> updated = bitapsAddressTransactionsProvider.get(
                new TransactionsRequestKey(ADDRESS_TRANSACTIONS, LAST_CHECKED_AT_BLOCK_HEIGHT)
        );

        assertThat(updated).contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void getName() {
        assertThat(bitapsAddressTransactionsProvider.getName()).isEqualTo("BitapsAddressTransactionsProvider");
    }
}