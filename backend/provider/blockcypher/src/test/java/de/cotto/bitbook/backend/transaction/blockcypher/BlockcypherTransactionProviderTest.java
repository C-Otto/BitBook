package de.cotto.bitbook.backend.transaction.blockcypher;

import de.cotto.bitbook.backend.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.blockcypher.BlockcypherTransactionDtoFixtures.BLOCKCYPHER_TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SuppressWarnings("CPD-START")
@ExtendWith(MockitoExtension.class)
class BlockcypherTransactionProviderTest {

    @InjectMocks
    private BlockcypherTransactionProvider provider;

    @Mock
    private BlockcypherClient blockcypherClient;

    @Test
    void getTransaction() {
        when(blockcypherClient.getTransaction(TRANSACTION_HASH))
                .thenReturn(Optional.of(BLOCKCYPHER_TRANSACTION));
        Optional<Transaction> transaction = provider.get(TRANSACTION_HASH);
        assertThat(transaction).contains(TRANSACTION);
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("BlockcypherTransactionProvider");
    }
}