package de.cotto.bitbook.backend.transaction.blockchaininfo;

import de.cotto.bitbook.backend.ProviderException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("CPD-START")
@ExtendWith(MockitoExtension.class)
class BlockchainInfoBlockHeightProviderTest {

    @InjectMocks
    private BlockchainInfoBlockHeightProvider provider;

    @Mock
    private BlockchainInfoClient blockchainInfoClient;

    @Test
    void isSupported_btc() {
        assertThat(provider.isSupported(BTC)).isTrue();
    }

    @Test
    void isSupported_bch() {
        assertThat(provider.isSupported(BCH)).isFalse();
    }

    @Test
    void get_unsupported_chain() {
        assertThatExceptionOfType(ProviderException.class).isThrownBy(() -> provider.get(BCH));
        verifyNoInteractions(blockchainInfoClient);
    }

    @Test
    void get_weird_string() {
        when(blockchainInfoClient.getBlockHeight()).thenReturn("xxx");
        assertThatExceptionOfType(ProviderException.class).isThrownBy(() -> provider.get(BTC));
    }

    @Test
    void get_empty_string() {
        when(blockchainInfoClient.getBlockHeight()).thenReturn("");
        assertThatExceptionOfType(ProviderException.class).isThrownBy(() -> provider.get(BTC));
    }

    @Test
    void get() throws Exception {
        when(blockchainInfoClient.getBlockHeight()).thenReturn(String.valueOf(BLOCK_HEIGHT));
        assertThat(provider.get(BTC)).contains(BLOCK_HEIGHT);
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("BlockchainInfoBlockHeightProvider");
    }
}