package de.cotto.bitbook.backend.transaction.bitcoind;

import de.cotto.bitbook.backend.ProviderException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BitcoindBlockHeightProviderTest {

    @InjectMocks
    private BitcoindBlockHeightProvider provider;

    @Mock
    private BitcoinCliWrapper bitcoinCliWrapper;

    @Test
    void getBlockHeight_error() {
        when(bitcoinCliWrapper.getBlockCount()).thenReturn(Optional.empty());
        assertThatExceptionOfType(ProviderException.class).isThrownBy(provider::get);
    }

    @Test
    void getBlockHeight() throws Exception {
        when(bitcoinCliWrapper.getBlockCount()).thenReturn(Optional.of(BLOCK_HEIGHT));
        assertThat(provider.get()).contains(BLOCK_HEIGHT);
    }

    @Test
    void get_with_argument() throws Exception {
        when(bitcoinCliWrapper.getBlockCount()).thenReturn(Optional.of(BLOCK_HEIGHT));
        assertThat(provider.get("x")).contains(BLOCK_HEIGHT);
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("BitcoindBlockHeightProvider");
    }
}