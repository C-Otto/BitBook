package de.cotto.bitbook.backend.transaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockHeightServiceTest {
    @InjectMocks
    private BlockHeightService blockHeightService;

    @Mock
    private PrioritizingBlockHeightProvider prioritizingBlockHeightProvider;

    @Test
    void getBlockHeight() {
        when(prioritizingBlockHeightProvider.getBlockHeight()).thenReturn(123);
        assertThat(blockHeightService.getBlockHeight()).isEqualTo(123);
    }

    @Test
    void caches_block_count() {
        when(prioritizingBlockHeightProvider.getBlockHeight()).thenReturn(123);
        blockHeightService.getBlockHeight();
        blockHeightService.getBlockHeight();

        verify(prioritizingBlockHeightProvider, times(1)).getBlockHeight();
    }

    @Test
    void does_not_cache_block_count_on_failure() {
        when(prioritizingBlockHeightProvider.getBlockHeight()).thenReturn(-1);
        blockHeightService.getBlockHeight();

        when(prioritizingBlockHeightProvider.getBlockHeight()).thenReturn(123);
        int blockHeight = blockHeightService.getBlockHeight();
        assertThat(blockHeight).isEqualTo(123);
    }
}