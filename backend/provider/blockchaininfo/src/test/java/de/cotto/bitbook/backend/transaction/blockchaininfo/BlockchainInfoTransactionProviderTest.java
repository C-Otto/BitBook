package de.cotto.bitbook.backend.transaction.blockchaininfo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.blockchaininfo.BlockchainInfoTransactionDtoFixtures.BLOCKCHAIN_INFO_TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockchainInfoTransactionProviderTest {

    @InjectMocks
    private BlockchainInfoTransactionProvider provider;

    @Mock
    private BlockchainInfoClient blockchainInfoClient;

    @Test
    void getTransaction() {
        when(blockchainInfoClient.getTransaction(TRANSACTION_HASH))
                .thenReturn(Optional.of(BLOCKCHAIN_INFO_TRANSACTION));
        assertThat(provider.get(TRANSACTION_HASH)).contains(TRANSACTION);
    }

    @Test
    void getTransaction_empty() {
        when(blockchainInfoClient.getTransaction(TRANSACTION_HASH)).thenReturn(Optional.empty());
        assertThat(provider.get(TRANSACTION_HASH)).isEmpty();
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("BlockchainInfoTransactionProvider");
    }
}