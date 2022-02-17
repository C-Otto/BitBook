package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.ProviderException;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static de.cotto.bitbook.backend.model.Chain.BTC;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrioritizingBlockchainInfoBlockHeightProviderTest {
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    private PrioritizingBlockHeightProvider prioritizingBlockHeightProvider;

    @Mock
    private BlockHeightProvider blockHeightProvider1;

    @Mock
    private BlockHeightProvider blockHeightProvider2;

    private boolean requestInFlight;

    @BeforeEach
    void setUp() {
        prioritizingBlockHeightProvider =
                new PrioritizingBlockHeightProvider(List.of(blockHeightProvider1, blockHeightProvider2));
        lenient().when(blockHeightProvider1.isSupported(BTC)).thenReturn(true);
        lenient().when(blockHeightProvider2.isSupported(BTC)).thenReturn(true);
    }

    @Test
    void getBlockHeight() throws Exception {
        when(blockHeightProvider1.get(any())).thenReturn(Optional.of(123));
        workOnRequestsInBackground();
        assertThat(getHeight()).isEqualTo(123);
    }

    @Test
    void all_fail() throws Exception {
        when(blockHeightProvider1.get(any())).thenThrow(FeignException.class);
        when(blockHeightProvider2.get(any())).thenThrow(ProviderException.class);
        workOnRequestsInBackground();
        assertThat(getHeight()).isEqualTo(-1);
    }

    @Test
    void getProvidedResultName() {
        assertThat(prioritizingBlockHeightProvider.getProvidedResultName()).isEqualTo("Block height");
    }

    private int getHeight() {
        requestInFlight = true;
        return prioritizingBlockHeightProvider.getBlockHeight(BTC);
    }

    private void workOnRequestsInBackground() {
        executor.execute(() -> {
            await().atMost(1, SECONDS).until(() -> requestInFlight);
            prioritizingBlockHeightProvider.workOnRequests();
        });
    }
}