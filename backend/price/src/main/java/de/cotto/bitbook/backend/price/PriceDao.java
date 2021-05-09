package de.cotto.bitbook.backend.price;

import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceWithDate;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

public interface PriceDao {
    Optional<Price> getPrice(LocalDate date);

    void savePrices(Collection<PriceWithDate> prices);
}
