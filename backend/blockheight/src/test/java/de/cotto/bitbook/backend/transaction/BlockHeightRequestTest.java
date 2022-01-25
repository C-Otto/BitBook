package de.cotto.bitbook.backend.transaction;

import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;

class BlockHeightRequestTest {
    @Test
    void getPriority() {
        assertThat(new BlockHeightRequest(BTC).getPriority()).isEqualTo(STANDARD);
    }

    @Test
    void getKey() {
        assertThat(new BlockHeightRequest(BCH).getKey()).isEqualTo(BCH);
    }
}