package de.cotto.bitbook.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomPromptProviderTest {

    @InjectMocks
    private CustomPromptProvider customPromptProvider;

    @Mock
    private SelectedChain selectedChain;

    @Test
    void customPrompt_text_btc() {
        when(selectedChain.getChain()).thenReturn(BTC);
        assertThat(customPromptProvider.getPrompt()).hasToString("BitBook₿ ");
    }

    @Test
    void customPrompt_text_bch() {
        when(selectedChain.getChain()).thenReturn(BCH);
        assertThat(customPromptProvider.getPrompt()).hasToString("BitBook(BCH)₿ ");
    }
}