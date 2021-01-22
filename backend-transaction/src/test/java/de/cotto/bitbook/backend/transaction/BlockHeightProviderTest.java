package de.cotto.bitbook.backend.transaction;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BlockHeightProviderTest {

    private final BlockHeightProvider provider = new TestableBlockHeightProvider();

    @Test
    void get_with_argument() {
        assertThat(provider.get("foo")).contains(123);
    }

    private static class TestableBlockHeightProvider implements BlockHeightProvider {
        @Override
        public String getName() {
            return "x";
        }

        @Override
        public Optional<Integer> get() {
            return Optional.of(123);
        }
    }
}