package de.cotto.bitbook.cli;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressConverterTest {
    @Test
    void convert() {
        assertThat(new AddressConverter().convert("x")).isEqualTo(new CliAddress("x"));
    }
}