package de.cotto.bitbook.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SelectedChainTest {
    @InjectMocks
    private SelectedChain selectedChain;

    @Test
    void initially_btc() {
        assertThat(selectedChain.getChain()).isEqualTo(BTC);
    }

    @Test
    void selectChain_bch() {
        selectedChain.selectChain(BCH);
        assertThat(selectedChain.getChain()).isEqualTo(BCH);
    }
}