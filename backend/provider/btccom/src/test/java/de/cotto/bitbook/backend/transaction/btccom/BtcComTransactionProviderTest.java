package de.cotto.bitbook.backend.transaction.btccom;

import de.cotto.bitbook.backend.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.btccom.BtcComTransactionDtoFixtures.BTCCOM_TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BtcComTransactionProviderTest {

    @InjectMocks
    private BtcComTransactionProvider provider;

    @Mock
    private BtcComClient btcComClient;

    @Test
    void getTransaction() {
        when(btcComClient.getTransaction(TRANSACTION_HASH))
                .thenReturn(Optional.of(BTCCOM_TRANSACTION));
        Optional<Transaction> transaction = provider.get(TRANSACTION_HASH);
        assertThat(transaction).contains(TRANSACTION);
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("BtcComTransactionProvider");
    }
}