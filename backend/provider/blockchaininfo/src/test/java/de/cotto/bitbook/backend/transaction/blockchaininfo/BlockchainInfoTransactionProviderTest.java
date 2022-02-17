package de.cotto.bitbook.backend.transaction.blockchaininfo;

import de.cotto.bitbook.backend.model.HashAndChain;
import de.cotto.bitbook.backend.model.ProviderException;
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
import static de.cotto.bitbook.backend.transaction.blockchaininfo.BlockchainInfoTransactionDtoFixtures.BLOCKCHAIN_INFO_TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@SuppressWarnings("CPD-START")
@ExtendWith(MockitoExtension.class)
class BlockchainInfoTransactionProviderTest {

    @InjectMocks
    private BlockchainInfoTransactionProvider provider;

    @Mock
    private BlockchainInfoClient blockchainInfoClient;

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
        when(blockchainInfoClient.getTransaction(TRANSACTION_HASH))
                .thenReturn(Optional.of(BLOCKCHAIN_INFO_TRANSACTION));
        assertThat(provider.get(new HashAndChain(TRANSACTION_HASH, BTC))).contains(TRANSACTION);
    }

    @Test
    void getTransaction_empty() throws Exception {
        when(blockchainInfoClient.getTransaction(TRANSACTION_HASH)).thenReturn(Optional.empty());
        assertThat(provider.get(new HashAndChain(TRANSACTION_HASH, BTC))).isEmpty();
    }

    @Test
    void getTransaction_unsupported() {
        assertThatExceptionOfType(ProviderException.class).isThrownBy(
                () -> provider.get(new HashAndChain(TRANSACTION_HASH, BCH))
        );
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("BlockchainInfoTransactionProvider");
    }
}