package de.cotto.bitbook.backend.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultFromProviderTest {
    @Test
    void failure() {
        assertThat(ResultFromProvider.<String>failure().isSuccessful()).isFalse();
    }

    @Test
    void success() {
        ResultFromProvider<String> success = ResultFromProvider.of("x");
        assertThat(success.isSuccessful()).isTrue();
        assertThat(success.getAsOptional()).contains("x");
    }

    @Test
    void empty() {
        ResultFromProvider<String> success = ResultFromProvider.empty();
        assertThat(success.isSuccessful()).isTrue();
        assertThat(success.getAsOptional()).isEmpty();
    }
}