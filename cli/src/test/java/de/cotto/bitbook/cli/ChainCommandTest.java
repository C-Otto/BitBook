package de.cotto.bitbook.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.Chain.BTG;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChainCommandTest {
    @InjectMocks
    private ChainCommand chainCommand;

    @Mock
    private SelectedChain selectedChain;

    @Test
    void selectChain_btc() {
        chainCommand.selectChain(BTC);
        verify(selectedChain).selectChain(BTC);
    }

    @Test
    void selectChain_btg() {
        chainCommand.selectChain(BTG);
        verify(selectedChain).selectChain(BTG);
    }
}