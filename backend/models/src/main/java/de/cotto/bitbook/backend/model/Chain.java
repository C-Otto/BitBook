package de.cotto.bitbook.backend.model;

import javax.annotation.Nullable;
import java.time.LocalDate;

public enum Chain {
    BTC(null, LocalDate.of(2009, 1, 3)),
    BCH(BTC, LocalDate.of(2017, 8, 1)),
    BTG(BTC, LocalDate.of(2017, 10, 24)),
    BCD(BTC, LocalDate.of(2017, 11, 24)),
    BSV(BCH, LocalDate.of(2018, 11, 15));

    @Nullable
    private final Chain originalChain;
    private final LocalDate forkDate;

    Chain(@Nullable Chain originalChain, LocalDate forkDate) {
        this.originalChain = originalChain;
        this.forkDate = forkDate;
    }

    public Chain getChainForDate(LocalDate date) {
        if (date.isBefore(forkDate) && originalChain != null) {
            return originalChain.getChainForDate(date);
        }
        return this;
    }
}
