package de.cotto.bitbook.backend.transaction.blockchaininfo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static org.assertj.core.api.Assertions.assertThat;
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
        assertThat(provider.get()).isEmpty();
    }

    @Test
    void getBlockHeight() {
        when(blockchainInfoClient.getBlockHeight()).thenReturn(String.valueOf(BLOCK_HEIGHT));
        assertThat(provider.get()).contains(BLOCK_HEIGHT);
    }

    @Test
    void get_with_argument() {
        when(blockchainInfoClient.getBlockHeight()).thenReturn(String.valueOf(BLOCK_HEIGHT));
        assertThat(provider.get("x")).contains(BLOCK_HEIGHT);
    }

    @Test
    void getName() {
        assertThat(provider.getName()).isEqualTo("BlockchainInfoBlockHeightProvider");
    }
}