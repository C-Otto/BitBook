package de.cotto.bitbook.backend.price;

import de.cotto.bitbook.backend.model.Provider;
import de.cotto.bitbook.backend.price.model.PriceContext;
import de.cotto.bitbook.backend.price.model.PriceWithContext;

import java.util.Collection;

public abstract class PriceProvider implements Provider<PriceContext, Collection<PriceWithContext>> {
    protected PriceProvider() {
        // just used for tests
    }
}
