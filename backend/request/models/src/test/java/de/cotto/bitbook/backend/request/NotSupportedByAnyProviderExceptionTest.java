package de.cotto.bitbook.backend.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotSupportedByAnyProviderExceptionTest {
    @Test
    void isException() {
        assertThat(new NotSupportedByAnyProviderException()).isInstanceOf(Exception.class);
    }
}