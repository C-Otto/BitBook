package de.cotto.bitbook.backend.price;

import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceWithDate;
import de.cotto.bitbook.backend.request.ResultFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.toMap;

@Component
public class PriceService {
    private final PrioritizingPriceProvider priceProvider;
    private final PriceDao priceDao;

    public PriceService(PrioritizingPriceProvider priceProvider, PriceDao priceDao) {
        this.priceProvider = priceProvider;
        this.priceDao = priceDao;
    }

    public Price getPrice(LocalDateTime dateTime) {
        return getPrice(PriceRequest.forDateStandardPriority(dateTime.toLocalDate()));
    }

    public Price getPrice(PriceRequest priceRequest) {
        Future<Price> priceFuture = getPriceFuture(priceRequest);
        return ResultFuture.getOrElse(priceFuture, Price.UNKNOWN);
    }

    public Map<LocalDate, Price> getPrices(Set<LocalDateTime> dates) {
        return dates.stream().collect(toMap(LocalDateTime::toLocalDate, this::getPrice));
    }

    private Future<Price> getPriceFuture(PriceRequest priceRequest) {
        Price persistedPrice = priceDao.getPrice(priceRequest.getDate()).orElse(null);
        if (persistedPrice == null) {
            return priceProvider.getPrices(priceRequest).getFuture()
                    .thenApply(result -> {
                        priceDao.savePrices(result);
                        return result;
                    })
                    .thenApply(set -> getForDate(set, priceRequest.getDate()));
        }
        return CompletableFuture.completedFuture(persistedPrice);
    }

    public Price getCurrentPrice() {
        return getPrice(PriceRequest.forCurrentPrice());
    }

    @Async
    public void requestPriceInBackground(LocalDateTime dateTime) {
        getPrice(PriceRequest.forDateLowestPriority(dateTime.toLocalDate()));
    }

    private Price getForDate(Collection<PriceWithDate> priceWithDates, LocalDate date) {
        return priceWithDates.stream()
                .filter(priceWithDate -> date.equals(priceWithDate.getDate()))
                .map(PriceWithDate::getPrice)
                .findFirst()
                .orElse(Price.UNKNOWN);
    }
}
