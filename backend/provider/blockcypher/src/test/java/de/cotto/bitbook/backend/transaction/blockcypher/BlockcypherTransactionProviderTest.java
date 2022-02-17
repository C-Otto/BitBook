package de.cotto.bitbook.backend.transaction.blockcypher;

import de.cotto.bitbook.backend.model.HashAndChain;
import de.cotto.bitbook.backend.model.ProviderException;
import de.cotto.bitbook.backend.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.blockcypher.BlockcypherTransactionDtoFixtures.BLOCKCYPHER_TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@SuppressWarnings("CPD-START")
@ExtendWith(MockitoExtension.class)
class BlockcypherTransactionProviderTest {

    @InjectMocks
    private BlockcypherTransactionProvider provider;

    @Mock
    private BlockcypherClient blockcypherClient;

    @Test
    void isSupported_btc() {
        assertThat(provider.isSupported(new HashAndChain(TRANSACTION_HASH, BTC))).isTrue();
    }

    @Test
    void isSupported_bch() {
        assertThat(provider.isSupported(new HashAndChain(TRANSACTION_HASH, BCH))).isFalse();
    }

    @Test
    void getTransaction() throws Exception {
        when(blockcypherClient.getTransaction(TRANSACTION_HASH))
                .thenReturn(Optional.of(BLOCKCYPHER_TRANSACTION));
        Optional<Transaction> transaction = provider.get(new HashAndChain(TRANSACTION_HASH, BTC));
        assertThat(transaction).contains(TRANSACTION);
    }

    @Test
    void getTransaction_unsupported() {
        assertThatExceptionOfType(ProviderException.class).isThrownBy(
                () -> provider.get(new HashAndChain(TRANSACTION_HASH, BCH))
        );
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("BlockcypherTransactionProvider");
    }
}