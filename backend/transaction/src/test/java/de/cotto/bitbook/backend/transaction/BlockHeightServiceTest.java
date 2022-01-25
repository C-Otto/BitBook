package de.cotto.bitbook.backend.transaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BTC;
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
        when(prioritizingBlockHeightProvider.getBlockHeight(BTC)).thenReturn(123);
        assertThat(blockHeightService.getBlockHeight(BTC)).isEqualTo(123);
    }

    @Test
    void caches_block_height() {
        when(prioritizingBlockHeightProvider.getBlockHeight(BTC)).thenReturn(123);
        blockHeightService.getBlockHeight(BTC);
        blockHeightService.getBlockHeight(BTC);

        verify(prioritizingBlockHeightProvider, times(1)).getBlockHeight(BTC);
    }

    @Test
    void does_not_cache_height_count_lower_than_previous_height() {
        when(prioritizingBlockHeightProvider.getBlockHeight(BTC)).thenReturn(123).thenReturn(100);
        when(prioritizingBlockHeightProvider.getBlockHeight(BCH)).thenReturn(900);
        blockHeightService.getBlockHeight(BTC);
        blockHeightService.blockHeightCache.getUnchecked(BCH); // this removes the value for key BTC
        blockHeightService.blockHeightCache.cleanUp();

        assertThat(blockHeightService.getBlockHeight(BTC)).isEqualTo(123);
        verify(prioritizingBlockHeightProvider, times(2)).getBlockHeight(BTC);
    }

    @Test
    void does_not_cache_block_count_on_failure() {
        when(prioritizingBlockHeightProvider.getBlockHeight(BTC)).thenReturn(-1);
        blockHeightService.getBlockHeight(BTC);

        when(prioritizingBlockHeightProvider.getBlockHeight(BTC)).thenReturn(123);
        int blockHeight = blockHeightService.getBlockHeight(BTC);
        assertThat(blockHeight).isEqualTo(123);
    }
}