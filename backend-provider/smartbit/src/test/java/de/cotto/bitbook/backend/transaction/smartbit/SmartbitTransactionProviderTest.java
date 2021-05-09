package de.cotto.bitbook.backend.transaction.smartbit;

import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.smartbit.SmartbitTransactionDtoFixtures.SMARTBIT_TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmartbitTransactionProviderTest {

    @InjectMocks
    private SmartbitTransactionProvider provider;

    @Mock
    private SmartbitClient smartbitClient;

    @Test
    void getTransaction() {
        when(smartbitClient.getTransaction(TRANSACTION_HASH))
                .thenReturn(Optional.of(SMARTBIT_TRANSACTION));
        Optional<Transaction> transaction = provider.get(TRANSACTION_HASH);
        assertThat(transaction).contains(TRANSACTION);
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("SmartbitTransactionProvider");
    }
}