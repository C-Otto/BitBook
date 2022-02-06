package de.cotto.bitbook.backend.transaction.blockchair;

import de.cotto.bitbook.backend.model.ProviderException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.Chain.BTG;
import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("CPD-START")
@ExtendWith(MockitoExtension.class)
class BlockchairBlockHeightProviderTest {

    @InjectMocks
    private BlockchairBlockHeightProvider provider;

    @Mock
    private BlockchairClient blockchairClient;

    @Test
    void isSupported_btc() {
        assertThat(provider.isSupported(BTC)).isTrue();
    }

    @Test
    void isSupported_bch() {
        assertThat(provider.isSupported(BCH)).isTrue();
    }

    @Test
    void isSupported_btg() {
        assertThat(provider.isSupported(BTG)).isFalse();
    }

    @Test
    void get_unsupported_chain() {
        assertThatExceptionOfType(ProviderException.class).isThrownBy(() -> provider.get(BTG));
        verifyNoInteractions(blockchairClient);
    }

    @Test
    void get_error() {
        when(blockchairClient.getBlockHeight(anyString())).thenReturn(Optional.empty());
        assertThatExceptionOfType(ProviderException.class).isThrownBy(() -> provider.get(BTC));
    }

    @Test
    void get_btc() throws Exception {
        when(blockchairClient.getBlockHeight("bitcoin"))
                .thenReturn(Optional.of(new BlockchairBlockHeightDto(BLOCK_HEIGHT)));
        assertThat(provider.get(BTC)).contains(BLOCK_HEIGHT);
    }

    @Test
    void get_bch() throws Exception {
        when(blockchairClient.getBlockHeight("bitcoin-cash"))
                .thenReturn(Optional.of(new BlockchairBlockHeightDto(BLOCK_HEIGHT)));
        assertThat(provider.get(BCH)).contains(BLOCK_HEIGHT);
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("BlockchairBlockHeightProvider");
    }
}