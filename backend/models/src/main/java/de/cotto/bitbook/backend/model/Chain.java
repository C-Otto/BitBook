package de.cotto.bitbook.backend.model;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.Objects;

public enum Chain {
    BTC("Bitcoin", 0, null, LocalDate.of(2009, 1, 3)),
    BCH("Bitcoin Cash", 478_559, BTC, LocalDate.of(2017, 8, 1)),
    BTG("Bitcoin Gold", 491_407, BTC, LocalDate.of(2017, 10, 24)),
    BCD("Bitcoin Diamond", 495_867, BTC, LocalDate.of(2017, 11, 24)),
    BSV("Bitcoin SV", 556_767, BCH, LocalDate.of(2018, 11, 15));

    private final String name;
    private final int firstBlockAfterFork;

    @Nullable
    private final Chain originalChain;
    private final LocalDate forkDate;

    Chain(String name, int firstBlockAfterFork, @Nullable Chain originalChain, LocalDate forkDate) {
        this.name = name;
        this.firstBlockAfterFork = firstBlockAfterFork;
        this.originalChain = originalChain;
        this.forkDate = forkDate;
    }

    public String getName() {
        return name;
    }

    public int getFirstBlockAfterFork() {
        return firstBlockAfterFork;
    }

    public Chain getChainForDate(LocalDate date) {
        if (date.isBefore(forkDate) && originalChain != null) {
            return originalChain.getChainForDate(date);
        }
        return this;
    }

    public Chain getChainForBlockHeight(int height) {
        if (height >= firstBlockAfterFork) {
            return this;
        }
        return Objects.requireNonNull(originalChain).getChainForBlockHeight(height);
    }

}
