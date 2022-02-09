package de.cotto.bitbook.backend.transaction.blockchair;

import de.cotto.bitbook.backend.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.blockchair.BlockchairTransactionDtoFixtures.BLOCKCHAIR_TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockchairTransactionProviderTest {

    @InjectMocks
    private BlockchairTransactionProvider provider;

    @Mock
    private BlockchairClient blockchairClient;

    @Test
    void getTransaction() {
        when(blockchairClient.getTransaction(TRANSACTION_HASH))
                .thenReturn(Optional.of(BLOCKCHAIR_TRANSACTION));
        Optional<Transaction> transaction = provider.get(TRANSACTION_HASH);
        assertThat(transaction).contains(TRANSACTION);
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("BlockchairTransactionProvider");
    }
}