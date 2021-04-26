package de.cotto.bitbook.backend.transaction;

import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;

class BlockHeightRequestTest {
    @Test
    void getPriority() {
        assertThat(BlockHeightRequest.STANDARD_PRIORITY.getPriority()).isEqualTo(STANDARD);
    }

    @Test
    void getKey() {
        assertThat(BlockHeightRequest.STANDARD_PRIORITY.getKey()).isEqualTo("");
    }
}