package de.cotto.bitbook.backend.model;

import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.Chain.BTC;
import static org.assertj.core.api.Assertions.assertThat;

class ChainTest {
    @Test
    void btc() {
        assertThat(BTC).hasToString("BTC");
        assertThat(Chain.valueOf("BTC")).isEqualTo(BTC);
    }
}