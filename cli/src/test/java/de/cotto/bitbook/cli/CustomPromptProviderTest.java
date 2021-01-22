package de.cotto.bitbook.cli;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomPromptProviderTest {

    private final CustomPromptProvider customPromptProvider = new CustomPromptProvider();

    @Test
    void customPrompt_text() {
        assertThat(customPromptProvider.getPrompt()).hasToString("BitBook$ ");
    }
}