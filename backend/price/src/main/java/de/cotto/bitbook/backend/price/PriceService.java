package de.cotto.bitbook.backend.price;

import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceContext;
import de.cotto.bitbook.backend.price.model.PriceWithContext;
import de.cotto.bitbook.backend.request.ResultFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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

    public Price getCurrentPrice(Chain chain) {
        return getPrice(PriceRequest.forCurrentPrice(chain));
    }

    @Async
    public void requestPriceInBackground(LocalDateTime dateTime, Chain chain) {
        getPrice(PriceRequest.createWithLowestPriority(new PriceContext(dateTime.toLocalDate(), chain)));
    }

    public Map<PriceContext, Price> getPrices(Set<LocalDateTime> dates, Chain chain) {
        Map<PriceContext, Future<Price>> futures = dates.stream()
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .map(date -> new PriceContext(date, chain))
                .map(PriceRequest::createWithStandardPriority)
                .collect(toMap(PriceRequest::getPriceContext, this::getPriceFuture));
        return futures.entrySet().stream().collect(toMap(
                Map.Entry::getKey,
                entry -> ResultFuture.getOrElse(entry.getValue(), Price.UNKNOWN)
        ));
    }

    public Price getPrice(LocalDateTime dateTime, Chain chain) {
        return getPrice(PriceRequest.createWithStandardPriority(new PriceContext(dateTime.toLocalDate(), chain)));
    }

    private Price getPrice(PriceRequest priceRequest) {
        Future<Price> priceFuture = getPriceFuture(priceRequest);
        return ResultFuture.getOrElse(priceFuture, Price.UNKNOWN);
    }

    private Future<Price> getPriceFuture(PriceRequest priceRequest) {
        Price persistedPrice = priceDao.getPrice(priceRequest.getPriceContext()).orElse(null);
        if (persistedPrice == null) {
            return priceProvider.getPrices(priceRequest).getFuture()
                    .thenApply(result -> {
                        priceDao.savePrices(result);
                        return result;
                    })
                    .thenApply(set -> getForContext(set, priceRequest.getPriceContext()));
        }
        return CompletableFuture.completedFuture(persistedPrice);
    }

    private Price getForContext(Collection<PriceWithContext> pricesWithContexts, PriceContext expectedContext) {
        return pricesWithContexts.stream()
                .filter(priceWithContext -> expectedContext.equals(priceWithContext.getPriceContext()))
                .map(PriceWithContext::getPrice)
                .findFirst()
                .orElse(Price.UNKNOWN);
    }
}
