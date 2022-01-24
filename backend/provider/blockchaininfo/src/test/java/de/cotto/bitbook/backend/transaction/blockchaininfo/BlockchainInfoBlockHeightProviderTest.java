package de.cotto.bitbook.backend.transaction.blockchaininfo;

import de.cotto.bitbook.backend.ProviderException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockchainInfoBlockHeightProviderTest {

    @InjectMocks
    private BlockchainInfoBlockHeightProvider provider;

    @Mock
    private BlockchainInfoClient blockchainInfoClient;

    @Test
    void getBlockHeight_weird_string() {
        when(blockchainInfoClient.getBlockHeight()).thenReturn("xxx");
        assertThatExceptionOfType(ProviderException.class).isThrownBy(provider::get);
    }

    @Test
    void getBlockHeight_empty_string() {
        when(blockchainInfoClient.getBlockHeight()).thenReturn("");
        assertThatExceptionOfType(ProviderException.class).isThrownBy(provider::get);
    }

    @Test
    void getBlockHeight() throws Exception {
        when(blockchainInfoClient.getBlockHeight()).thenReturn(String.valueOf(BLOCK_HEIGHT));
        assertThat(provider.get()).contains(BLOCK_HEIGHT);
    }

    @Test
    void get_with_argument() throws Exception {
        when(blockchainInfoClient.getBlockHeight()).thenReturn(String.valueOf(BLOCK_HEIGHT));
        assertThat(provider.get("x")).contains(BLOCK_HEIGHT);
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("BlockchainInfoBlockHeightProvider");
    }
}