package de.cotto.bitbook.backend.transaction.blockchair;

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
import static de.cotto.bitbook.backend.model.Chain.BSV;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.Chain.BTG;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_BCH;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_BSV;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.blockchair.BlockchairTransactionDtoFixtures.BLOCKCHAIR_TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockchairTransactionProviderTest {

    @InjectMocks
    private BlockchairTransactionProvider provider;

    @Mock
    private BlockchairClient blockchairClient;

    @Test
    void isSupported_btc() {
        assertThat(provider.isSupported(new HashAndChain(TRANSACTION_HASH, BTC))).isTrue();
    }

    @Test
    void isSupported_bch() {
        assertThat(provider.isSupported(new HashAndChain(TRANSACTION_HASH, BCH))).isTrue();
    }

    @Test
    void isSupported_bsv() {
        assertThat(provider.isSupported(new HashAndChain(TRANSACTION_HASH, BSV))).isTrue();
    }

    @Test
    void isSupported_btg() {
        assertThat(provider.isSupported(new HashAndChain(TRANSACTION_HASH, BTG))).isFalse();
    }

    @Test
    void getTransaction_btc() throws Exception {
        when(blockchairClient.getTransaction("bitcoin", TRANSACTION_HASH))
                .thenReturn(Optional.of(BLOCKCHAIR_TRANSACTION));
        Optional<Transaction> transaction = provider.get(new HashAndChain(TRANSACTION_HASH, BTC));
        assertThat(transaction).contains(TRANSACTION);
    }

    @Test
    void getTransaction_bch() throws Exception {
        when(blockchairClient.getTransaction("bitcoin-cash", TRANSACTION_HASH))
                .thenReturn(Optional.of(BLOCKCHAIR_TRANSACTION));
        Optional<Transaction> transaction = provider.get(new HashAndChain(TRANSACTION_HASH, BCH));
        assertThat(transaction).contains(TRANSACTION_BCH);
    }

    @Test
    void getTransaction_bsv() throws Exception {
        when(blockchairClient.getTransaction("bitcoin-sv", TRANSACTION_HASH))
                .thenReturn(Optional.of(BLOCKCHAIR_TRANSACTION));
        Optional<Transaction> transaction = provider.get(new HashAndChain(TRANSACTION_HASH, BSV));
        assertThat(transaction).contains(TRANSACTION_BSV);
    }

    @Test
    void getTransaction_unsupported() {
        assertThatExceptionOfType(ProviderException.class).isThrownBy(
                () -> provider.get(new HashAndChain(TRANSACTION_HASH, BTG))
        );
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("BlockchairTransactionProvider");
    }
}