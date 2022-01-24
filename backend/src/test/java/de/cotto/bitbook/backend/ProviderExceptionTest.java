package de.cotto.bitbook.backend;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderExceptionTest {
    @Test
    void isException() {
        assertThat(new ProviderException()).isInstanceOf(Exception.class);
    }

    @Test
    void withCause() {
        ArithmeticException cause = new ArithmeticException();
        assertThat(new ProviderException(cause)).hasCause(cause);
    }

    @Test
    void noParameters() {
        assertThat(new ProviderException()).isNotNull();
    }
}