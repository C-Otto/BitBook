package de.cotto.bitbook.backend.price;

import de.cotto.bitbook.backend.price.model.PriceWithDate;
import de.cotto.bitbook.backend.request.PrioritizedRequest;
import de.cotto.bitbook.backend.request.RequestPriority;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;

import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;

public final class PriceRequest extends PrioritizedRequest<LocalDate, Collection<PriceWithDate>> {
    private PriceRequest(LocalDate date, RequestPriority priority) {
        super(date, priority);
    }

    public static PriceRequest forDateStandardPriority(LocalDate date) {
        return new PriceRequest(date, STANDARD);
    }

    public static PriceRequest forDateLowestPriority(LocalDate date) {
        return new PriceRequest(date, LOWEST);
    }

    public static PriceRequest forCurrentPrice() {
        return new PriceRequest(LocalDate.now(ZoneOffset.UTC), STANDARD);
    }

    public LocalDate getDate() {
        return getKey();
    }

    @Override
    public String toString() {
        return "PriceRequest{" +
               "date=" + getKey() +
               ", priority=" + getPriority() +
               "}";
    }
}
