package de.cotto.bitbook.backend.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AllProvidersFailedExceptionTest {
    @Test
    void isException() {
        assertThat(new AllProvidersFailedException()).isInstanceOf(Exception.class);
    }
}