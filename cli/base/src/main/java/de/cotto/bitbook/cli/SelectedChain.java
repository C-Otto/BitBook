package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.model.Chain;
import org.springframework.stereotype.Component;

@Component
public class SelectedChain {
    public SelectedChain() {
        // default constructor
    }

    public Chain getChain() {
        return Chain.BTC;
    }
}
