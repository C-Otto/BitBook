package de.cotto.bitbook.backend;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeignConfigurationTest {
    @Test
    void coverage_test() {
        assertThat(new FeignConfiguration()).isNotNull();
    }
}