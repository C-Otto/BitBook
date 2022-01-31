package de.cotto.bitbook.backend.price.model;

import de.cotto.bitbook.backend.model.Chain;

import java.time.LocalDate;

public record PriceContext(LocalDate date, Chain chain) {
    public PriceContext(LocalDate date, Chain chain) {
        this.chain = chain.getChainForDate(date);
        this.date = date;
    }

}
