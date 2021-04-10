package de.cotto.bitbook.cli;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionHashConverterTest {
    @Test
    void convert() {
        assertThat(new TransactionHashConverter().convert("x")).isEqualTo(new CliTransactionHash("x"));
    }
}