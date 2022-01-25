package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.Chain;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.Chain.BCH;
import static org.assertj.core.api.Assertions.assertThat;

class BlockHeightProviderTest {

    private final BlockHeightProvider provider = new TestableBlockHeightProvider();

    @Test
    void get_with_argument() throws Exception {
        assertThat(provider.get(BCH)).contains(123);
    }

    private static class TestableBlockHeightProvider implements BlockHeightProvider {
        @Override
        public String getName() {
            return "x";
        }

        @Override
        public Optional<Integer> get(Chain chain) {
            return Optional.of(123);
        }
    }
}