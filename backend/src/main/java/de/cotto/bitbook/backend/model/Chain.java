package de.cotto.bitbook.backend.model;

import javax.annotation.Nullable;
import java.util.Objects;

public enum Chain {
    BTC("Bitcoin", 0, null),
    BCH("Bitcoin Cash", 478_559, BTC),
    BTG("Bitcoin Gold", 491_407, BTC),
    BCD("Bitcoin Diamond", 495_867, BTC),
    BSV("Bitcoin SV", 556_767, BCH);

    private final String name;
    private final int firstBlockAfterFork;

    @Nullable
    private final Chain forkedFrom;

    Chain(String name, int firstBlockAfterFork, @Nullable Chain forkedFrom) {
        this.name = name;
        this.firstBlockAfterFork = firstBlockAfterFork;
        this.forkedFrom = forkedFrom;
    }

    public String getName() {
        return name;
    }

    public int getFirstBlockAfterFork() {
        return firstBlockAfterFork;
    }

    public Chain getChainForBlockHeight(int height) {
        if (height >= firstBlockAfterFork) {
            return this;
        }
        return Objects.requireNonNull(forkedFrom).getChainForBlockHeight(height);
    }
}
