package de.cotto.bitbook.backend.transaction.electrs;

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
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_2;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElectrsAddressTransactionsProviderTest {

    @InjectMocks
    private ElectrsAddressTransactionsProvider provider;

    @Mock
    private ElectrsClient electrsClient;

    @Test
    void getAddressDetails() {
        when(electrsClient.getTransactionHashes(ADDRESS))
                .thenReturn(Optional.of(Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2)));
        assertThat(provider.get(new TransactionsRequestKey(ADDRESS, LAST_CHECKED_AT_BLOCK_HEIGHT)))
                .contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void getUpdates() {
        when(electrsClient.getTransactionHashes(ADDRESS)).thenReturn(Optional.of(Set.of(
                TRANSACTION_HASH,
                TRANSACTION_HASH_2,
                TRANSACTION_HASH_3,
                TRANSACTION_HASH_4
        )));
        TransactionsRequestKey transactionsRequestKey = new TransactionsRequestKey(
                ADDRESS_TRANSACTIONS,
                ADDRESS_TRANSACTIONS_UPDATED.getLastCheckedAtBlockHeight()
        );
        assertThat(provider.get(transactionsRequestKey)).contains(ADDRESS_TRANSACTIONS_UPDATED);
    }

    @Test
    void getUpdates_no_update_returned() {
        when(electrsClient.getTransactionHashes(ADDRESS)).thenReturn(Optional.empty());

        assertThat(provider.get(new TransactionsRequestKey(ADDRESS_TRANSACTIONS, LAST_CHECKED_AT_BLOCK_HEIGHT)))
                .contains(ADDRESS_TRANSACTIONS);
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("ElectrsAddressTransactionsProvider");
    }
}