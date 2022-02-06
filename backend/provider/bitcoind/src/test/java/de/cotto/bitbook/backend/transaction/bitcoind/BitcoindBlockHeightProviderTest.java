package de.cotto.bitbook.backend.transaction.bitcoind;

import de.cotto.bitbook.backend.model.ProviderException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("CPD-START")
@ExtendWith(MockitoExtension.class)
class BitcoindBlockHeightProviderTest {

    @InjectMocks
    private BitcoindBlockHeightProvider provider;

    @Mock
    private BitcoinCliWrapper bitcoinCliWrapper;

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
        verifyNoInteractions(bitcoinCliWrapper);
    }

    @Test
    void get_error() {
        when(bitcoinCliWrapper.getBlockCount()).thenReturn(Optional.empty());
        assertThatExceptionOfType(ProviderException.class).isThrownBy(() -> provider.get(BTC));
    }

    @Test
    void get() throws Exception {
        when(bitcoinCliWrapper.getBlockCount()).thenReturn(Optional.of(BLOCK_HEIGHT));
        assertThat(provider.get(BTC)).contains(BLOCK_HEIGHT);
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("BitcoindBlockHeightProvider");
    }
}