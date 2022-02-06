package de.cotto.bitbook.backend.transaction.bitcoind;

import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.Optional;

import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static org.assertj.core.api.Assertions.assertThat;

class BitcoinCliWrapperTest {
    private final TestableBitcoinCliWrapper bitcoinCliWrapper = new TestableBitcoinCliWrapper();

    @Test
    void getBlockCount_error() {
        assertThat(bitcoinCliWrapper.getBlockCount()).isEmpty();
    }

    @Test
    void getBlockCount_empty_string() {
        bitcoinCliWrapper.result = "";
        assertThat(bitcoinCliWrapper.getBlockCount()).isEmpty();
    }

    @Test
    void getBlockCount() {
        bitcoinCliWrapper.result = BLOCK_HEIGHT + "\n";
        assertThat(bitcoinCliWrapper.getBlockCount()).contains(BLOCK_HEIGHT);
        assertThat(bitcoinCliWrapper.command).contains("bitcoin-cli", "getblockcount");
    }

    @SuppressWarnings("PMD.ArrayIsStoredDirectly")
    private static class TestableBitcoinCliWrapper extends BitcoinCliWrapper {
        @Nullable
        private String result;

        @Nullable
        private String[] command;

        @Override
        protected Optional<String> execute(String... command) {
            this.command = command;
            return Optional.ofNullable(result);
        }
    }
}