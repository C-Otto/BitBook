package de.cotto.bitbook.backend.transaction.blockstream;

import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.transaction.TransactionsRequestKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS_UPDATED;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.transaction.blockstream.BlockstreamAddressTransactionsFixtures.BLOCKSTREAM_ADDRESS_DETAILS;
import static de.cotto.bitbook.backend.transaction.blockstream.BlockstreamAddressTransactionsFixtures.BLOCKSTREAM_ADDRESS_UPDATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SuppressWarnings("CPD-START")
@ExtendWith(MockitoExtension.class)
class BlockstreamAddressTransactionsProviderTest {

    @InjectMocks
    private BlockstreamAddressTransactionsProvider provider;

    @Mock
    private BlockstreamInfoClient blockstreamInfoClient;

    @Test
    void isSupported_btc() {
        TransactionsRequestKey key = new TransactionsRequestKey(ADDRESS, BTC, 123);
        assertThat(provider.isSupported(key)).isTrue();
    }

    @Test
    void isSupported_bch() {
        TransactionsRequestKey key = new TransactionsRequestKey(ADDRESS, BCH, 666);
        assertThat(provider.isSupported(key)).isFalse();
    }

    @Test
    void getAddressDetails() throws Exception {
        when(blockstreamInfoClient.getAddressDetails(ADDRESS)).thenReturn(Optional.of(BLOCKSTREAM_ADDRESS_DETAILS));
        Optional<AddressTransactions> addressTransactions = provider.get(
                new TransactionsRequestKey(ADDRESS, BTC, LAST_CHECKED_AT_BLOCK_HEIGHT)
        );
        assertThat(addressTransactions).contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void getUpdates() throws Exception {
        when(blockstreamInfoClient.getAddressDetails(ADDRESS)).thenReturn(Optional.of(BLOCKSTREAM_ADDRESS_UPDATED));
        TransactionsRequestKey transactionsRequestKey = new TransactionsRequestKey(
                ADDRESS_TRANSACTIONS,
                ADDRESS_TRANSACTIONS_UPDATED.getLastCheckedAtBlockHeight()
        );
        Optional<AddressTransactions> updated =
                provider.get(transactionsRequestKey);
        assertThat(updated).contains(ADDRESS_TRANSACTIONS_UPDATED);
    }

    @Test
    void getUpdates_no_update_returned() throws Exception {
        when(blockstreamInfoClient.getAddressDetails(ADDRESS)).thenReturn(Optional.empty());

        Optional<AddressTransactions> updated = provider.get(
                new TransactionsRequestKey(ADDRESS_TRANSACTIONS, LAST_CHECKED_AT_BLOCK_HEIGHT)
        );

        assertThat(updated).contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("BlockstreamInfoAddressTransactionsProvider");
    }
}