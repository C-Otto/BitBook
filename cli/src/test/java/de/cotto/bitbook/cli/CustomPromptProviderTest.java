package de.cotto.bitbook.cli;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomPromptProviderTest {

    private final CustomPromptProvider customPromptProvider = new CustomPromptProvider();

    @Test
    void default_prompt() {
        assertThat(customPromptProvider.getPrompt()).hasToString("BitBook$ ");
    }

    @Test
    void changePrompt() {
        customPromptProvider.changePrompt("foo");
        assertThat(customPromptProvider.getPrompt()).hasToString("foo");
    }

    @Test
    void changePromptToDefault() {
        customPromptProvider.changePrompt("foo");
        customPromptProvider.changePromptToDefault();
        assertThat(customPromptProvider.getPrompt()).hasToString("BitBook$ ");
    }
}