package de.cotto.bitbook.backend.price;

import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.price.model.PriceContext;
import de.cotto.bitbook.backend.price.model.PriceWithContext;
import de.cotto.bitbook.backend.request.PrioritizedRequest;
import de.cotto.bitbook.backend.request.RequestPriority;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;

import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;

public final class PriceRequest extends PrioritizedRequest<PriceContext, Collection<PriceWithContext>> {
    private PriceRequest(PriceContext priceContext, RequestPriority priority) {
        super(priceContext, priority);
    }

    public static PriceRequest createWithStandardPriority(PriceContext priceContext) {
        return new PriceRequest(priceContext, STANDARD);
    }

    public static PriceRequest createWithLowestPriority(PriceContext priceContext) {
        return new PriceRequest(priceContext, LOWEST);
    }

    public static PriceRequest forCurrentPrice(Chain chain) {
        PriceContext priceContext = new PriceContext(LocalDate.now(ZoneOffset.UTC), chain);
        return new PriceRequest(priceContext, STANDARD);
    }

    public PriceContext getPriceContext() {
        return getKey();
    }

    @Override
    public String toString() {
        return "PriceRequest{" +
               "priceContext=" + getKey() +
               ", priority=" + getPriority() +
               "}";
    }
}
