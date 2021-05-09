package de.cotto.bitbook.cli;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class CustomPromptProvider implements PromptProvider, PromptChangeListener {
    private static final String DEFAULT_PROMPT = "BitBook$ ";

    private String prompt = DEFAULT_PROMPT;

    public CustomPromptProvider() {
        // default constructor
    }

    @Override
    public AttributedString getPrompt() {
        return new AttributedString(prompt, AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    }

    @Override
    public void changePrompt(String newPrompt) {
        prompt = newPrompt;
    }

    @Override
    public void changePromptToDefault() {
        changePrompt(DEFAULT_PROMPT);
    }
}