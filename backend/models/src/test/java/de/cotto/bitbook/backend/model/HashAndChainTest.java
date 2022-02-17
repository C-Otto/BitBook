package de.cotto.bitbook.backend.model;

import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static org.assertj.core.api.Assertions.assertThat;

class HashAndChainTest {
    @Test
    void hash() {
        assertThat(new HashAndChain(TRANSACTION_HASH, BTC).hash()).isEqualTo(TRANSACTION_HASH);
    }

    @Test
    void chain() {
        assertThat(new HashAndChain(TRANSACTION_HASH, BTC).chain()).isEqualTo(BTC);
    }
}