package de.cotto.bitbook.backend.price;

import de.cotto.bitbook.backend.Provider;
import de.cotto.bitbook.backend.price.model.PriceWithDate;
import de.cotto.bitbook.backend.request.PrioritizingProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component
public class PrioritizingPriceProvider extends PrioritizingProvider<LocalDate, Collection<PriceWithDate>> {
    public PrioritizingPriceProvider(List<Provider<LocalDate, Collection<PriceWithDate>>> providers) {
        super(providers, "Price");
    }

    public Collection<PriceWithDate> getPrices(PriceRequest request) {
        return getForRequestBlocking(request).orElse(Set.of());
    }
}
