package de.cotto.bitbook.backend.transaction.blockchair;

import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BSV;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.Chain.BTG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BlockchairChainNameTest {
    @Test
    void btc() {
        assertThat(BlockchairChainName.get(BTC)).isEqualTo("bitcoin");
    }

    @Test
    void bch() {
        assertThat(BlockchairChainName.get(BCH)).isEqualTo("bitcoin-cash");
    }

    @Test
    void bsv() {
        assertThat(BlockchairChainName.get(BSV)).isEqualTo("bitcoin-sv");
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void unsupported() {
        assertThatIllegalArgumentException().isThrownBy(() -> BlockchairChainName.get(BTG));
    }
}