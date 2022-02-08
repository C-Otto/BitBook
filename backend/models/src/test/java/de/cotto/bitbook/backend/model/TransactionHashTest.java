package de.cotto.bitbook.backend.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionHashTest {

    private static final TransactionHash HASH_ABC = new TransactionHash("abc");

    @Test
    void testToString() {
        assertThat(HASH_ABC).hasToString("abc");
    }

    @Test
    void isInvalid() {
        assertThat(HASH_ABC.isInvalid()).isFalse();
    }

    @Test
    void isInvalid_empty() {
        assertThat(new TransactionHash("").isInvalid()).isTrue();
    }

    @Test
    void isInvalid_blank() {
        assertThat(new TransactionHash(" ").isInvalid()).isTrue();
    }

    @Test
    void isValid() {
        assertThat(HASH_ABC.isValid()).isTrue();
    }

    @Test
    void isValid_empty() {
        assertThat(new TransactionHash("").isValid()).isFalse();
    }

    @Test
    void isValid_blank() {
        assertThat(new TransactionHash(" ").isValid()).isFalse();
    }

    @Test
    void none() {
        assertThat(TransactionHash.NONE).isEqualTo(new TransactionHash(""));
    }

    @Test
    void comparable_smaller() {
        assertThat(new TransactionHash("a").compareTo(new TransactionHash("b"))).isNegative();
    }

    @Test
    void comparable_larger() {
        assertThat(new TransactionHash("b").compareTo(new TransactionHash("a"))).isPositive();
    }
}