package de.cotto.bitbook.backend.price;

import de.cotto.bitbook.backend.model.Provider;
import de.cotto.bitbook.backend.price.model.PriceContext;
import de.cotto.bitbook.backend.price.model.PriceWithContext;
import de.cotto.bitbook.backend.request.PrioritizingProvider;
import de.cotto.bitbook.backend.request.ResultFuture;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class PrioritizingPriceProvider extends PrioritizingProvider<PriceContext, Collection<PriceWithContext>> {
    public PrioritizingPriceProvider(List<Provider<PriceContext, Collection<PriceWithContext>>> providers) {
        super(providers, "Price");
    }

    public ResultFuture<Collection<PriceWithContext>> getPrices(PriceRequest request) {
        return getForRequest(request);
    }
}
