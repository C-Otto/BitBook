package de.cotto.bitbook.backend;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class ProviderTest {
    private final TestableProvider provider = new TestableProvider();

    @Test
    void supports_everything_by_default() {
        assertThat(provider.isSupported("abc")).isTrue();
    }

    @Test
    void isSupported_false() {
        assertThat(provider.isSupported("unsupported")).isFalse();
    }

    @Test
    void get() throws Exception {
        assertThat(provider.get("supported")).isNotEmpty();
    }

    @Test
    void throwIfUnsupported_supported() {
        assertThatCode(() -> provider.throwIfUnsupported("supported")).doesNotThrowAnyException();
    }

    @Test
    void throwIfUnsupported_unsupported() {
        assertThatExceptionOfType(ProviderException.class).isThrownBy(
                () -> provider.throwIfUnsupported("unsupported")
        );
    }
}