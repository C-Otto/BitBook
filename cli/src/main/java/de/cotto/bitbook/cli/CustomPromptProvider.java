package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.model.Chain;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

import static de.cotto.bitbook.backend.model.Chain.BTC;
import static org.jline.utils.AttributedStyle.BOLD;
import static org.jline.utils.AttributedStyle.DEFAULT;

@Component
public class CustomPromptProvider implements PromptProvider {

    private final SelectedChain selectedChain;

    public CustomPromptProvider(SelectedChain selectedChain) {
        this.selectedChain = selectedChain;
    }

    @Override
    public AttributedString getPrompt() {
        Chain chain = selectedChain.getChain();
        AttributedString prefix = new AttributedString("BitBook", DEFAULT.foreground(AttributedStyle.YELLOW));
        AttributedString middle;
        if (chain == BTC) {
            middle = AttributedString.EMPTY;
        } else {
            middle = new AttributedString("(%s)".formatted(chain.name()), BOLD.foreground(AttributedStyle.CYAN));
        }
        AttributedString suffix = new AttributedString("₿ ", DEFAULT.foreground(AttributedStyle.YELLOW));
        return AttributedString.join(AttributedString.EMPTY, prefix, middle, suffix);
    }

}