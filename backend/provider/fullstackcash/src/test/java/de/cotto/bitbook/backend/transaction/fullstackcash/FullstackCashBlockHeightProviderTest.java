package de.cotto.bitbook.backend.transaction.fullstackcash;

import de.cotto.bitbook.backend.model.ProviderException;
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
class FullstackCashBlockHeightProviderTest {

    @InjectMocks
    private FullstackCashBlockHeightProvider provider;

    @Mock
    private FullstackCashClient fullstackCashClient;

    @Test
    void isSupported_btc() {
        assertThat(provider.isSupported(BTC)).isFalse();
    }

    @Test
    void isSupported_bch() {
        assertThat(provider.isSupported(BCH)).isTrue();
    }

    @Test
    void get_unsupported_chain() {
        assertThatExceptionOfType(ProviderException.class).isThrownBy(() -> provider.get(BTC));
        verifyNoInteractions(fullstackCashClient);
    }

    @Test
    void get_weird_string() {
        when(fullstackCashClient.getBlockHeight()).thenReturn("xxx");
        assertThatExceptionOfType(ProviderException.class).isThrownBy(() -> provider.get(BCH));
    }

    @Test
    void get_empty_string() {
        when(fullstackCashClient.getBlockHeight()).thenReturn("");
        assertThatExceptionOfType(ProviderException.class).isThrownBy(() -> provider.get(BCH));
    }

    @Test
    void get() throws Exception {
        when(fullstackCashClient.getBlockHeight()).thenReturn(String.valueOf(BLOCK_HEIGHT));
        assertThat(provider.get(BCH)).contains(BLOCK_HEIGHT);
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("FullstackCashBlockHeightProvider");
    }
}