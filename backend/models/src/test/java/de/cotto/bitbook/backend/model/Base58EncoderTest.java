package de.cotto.bitbook.backend.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Base58EncoderTest {
    @Test
    void encode_leading_zeros() {
        assertThat(new Base58Encoder(new HexString("0000FF")).encode()).isEqualTo("115Q");
    }

    @Test
    void encode_zeros_not_leading() {
        assertThat(new Base58Encoder(new HexString("00FF00")).encode()).isEqualTo("1LQX");
    }
}