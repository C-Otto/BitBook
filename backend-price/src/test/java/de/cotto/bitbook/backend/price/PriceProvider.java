package de.cotto.bitbook.backend.price;

import de.cotto.bitbook.backend.Provider;
import de.cotto.bitbook.backend.price.model.PriceWithDate;

import java.time.LocalDate;
import java.util.Collection;

public abstract class PriceProvider implements Provider<LocalDate, Collection<PriceWithDate>> {
    protected PriceProvider() {
        // just used for tests
    }
}
