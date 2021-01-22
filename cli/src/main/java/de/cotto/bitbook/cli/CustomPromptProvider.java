package de.cotto.bitbook.cli;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class CustomPromptProvider implements PromptProvider {

    public CustomPromptProvider() {
        // default constructor
    }

    @Override
    public AttributedString getPrompt() {
        return new AttributedString("BitBook$ ", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    }

}