package de.cotto.bitbook.backend.price;

import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceWithDate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

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
        Optional<Price> persistedPrice = priceDao.getPrice(priceRequest.getDate());
        return persistedPrice.orElseGet(
                () -> getPriceFromPriceProviderAndPersist(priceRequest).stream()
                .filter(priceWithDate -> priceWithDate.getDate().equals(priceRequest.getDate()))
                .map(PriceWithDate::getPrice)
                .findFirst()
                .orElse(Price.UNKNOWN)
        );
    }

    public Price getCurrentPrice() {
        return getPrice(PriceRequest.forCurrentPrice());
    }

    @Async
    public void requestPriceInBackground(LocalDateTime dateTime) {
        getPrice(PriceRequest.forDateLowestPriority(dateTime.toLocalDate()));
    }

    private Collection<PriceWithDate> getPriceFromPriceProviderAndPersist(PriceRequest priceRequest) {
        PriceRequest withResultConsumer = priceRequest.getWithResultConsumer(priceDao::savePrices);
        return priceProvider.getPrices(withResultConsumer);
    }
}
