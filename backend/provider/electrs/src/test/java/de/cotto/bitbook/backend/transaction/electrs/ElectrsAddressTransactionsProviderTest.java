package de.cotto.bitbook.backend.transaction.electrs;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.ProviderException;
import de.cotto.bitbook.backend.transaction.TransactionsRequestKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS_UPDATED;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BSV;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_2;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElectrsAddressTransactionsProviderTest {

    @InjectMocks
    private ElectrsAddressTransactionsProvider provider;

    @Mock
    private ElectrsClient electrsClient;

    @Test
    void isSupported_btc() {
        TransactionsRequestKey key = new TransactionsRequestKey(new Address("abc)"), BTC, 123);
        assertThat(provider.isSupported(key)).isTrue();
    }

    @Test
    void isSupported_bch() {
        TransactionsRequestKey key = new TransactionsRequestKey(new Address("abc)"), BCH, 123);
        assertThat(provider.isSupported(key)).isFalse();
    }

    @Test
    void get_unsupported_chain() {
        assertThatExceptionOfType(ProviderException.class).isThrownBy(
                () -> provider.get(new TransactionsRequestKey(ADDRESS, BSV, LAST_CHECKED_AT_BLOCK_HEIGHT))
        );
    }

    @Test
    void getAddressDetails() throws Exception {
        when(electrsClient.getTransactionHashes(ADDRESS))
                .thenReturn(Optional.of(Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2)));
        assertThat(provider.get(new TransactionsRequestKey(ADDRESS, BTC, LAST_CHECKED_AT_BLOCK_HEIGHT)))
                .contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void getUpdates() throws Exception {
        when(electrsClient.getTransactionHashes(ADDRESS)).thenReturn(Optional.of(Set.of(
                TRANSACTION_HASH,
                TRANSACTION_HASH_2,
                TRANSACTION_HASH_3,
                TRANSACTION_HASH_4
        )));
        TransactionsRequestKey transactionsRequestKey = new TransactionsRequestKey(
                ADDRESS_TRANSACTIONS,
                ADDRESS_TRANSACTIONS_UPDATED.lastCheckedAtBlockHeight()
        );
        assertThat(provider.get(transactionsRequestKey)).contains(ADDRESS_TRANSACTIONS_UPDATED);
    }

    @Test
    void getUpdates_no_update_returned() throws Exception {
        when(electrsClient.getTransactionHashes(ADDRESS)).thenReturn(Optional.empty());

        assertThat(provider.get(new TransactionsRequestKey(ADDRESS_TRANSACTIONS, LAST_CHECKED_AT_BLOCK_HEIGHT)))
                .contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("ElectrsAddressTransactionsProvider");
    }
}