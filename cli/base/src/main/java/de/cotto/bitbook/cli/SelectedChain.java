package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.model.Chain;
import org.springframework.stereotype.Component;

import static de.cotto.bitbook.backend.model.Chain.BTC;

@Component
public class SelectedChain {
    private Chain chain;

    public SelectedChain() {
        chain = BTC;
    }

    public Chain getChain() {
        return chain;
    }

    public void selectChain(Chain chain) {
        this.chain = chain;
    }
}
