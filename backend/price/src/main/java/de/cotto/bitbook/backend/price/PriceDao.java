package de.cotto.bitbook.backend.price;

import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceContext;
import de.cotto.bitbook.backend.price.model.PriceWithContext;

import java.util.Collection;
import java.util.Optional;

public interface PriceDao {
    Optional<Price> getPrice(PriceContext priceContext);

    void savePrices(Collection<PriceWithContext> prices);
}
