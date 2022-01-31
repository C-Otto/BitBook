package de.cotto.bitbook.backend.price.kraken;

import de.cotto.bitbook.backend.Provider;
import de.cotto.bitbook.backend.ProviderException;
import de.cotto.bitbook.backend.price.kraken.KrakenTradesDto.Trade;
import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceContext;
import de.cotto.bitbook.backend.price.model.PriceWithContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.cotto.bitbook.backend.model.Chain.BTC;

@Component
public class KrakenPriceProvider implements Provider<PriceContext, Collection<PriceWithContext>> {
    private static final LocalDate NO_PRICE_BEFORE = LocalDate.of(2013, 9, 1);
    private static final long ONE_DAY_IN_SECONDS = 24 * 60 * 60;
    private final KrakenClient krakenClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public KrakenPriceProvider(KrakenClient krakenClient) {
        this.krakenClient = krakenClient;
    }

    @Override
    public String getName() {
        return "KrakenPriceProvider";
    }

    @Override
    public boolean isSupported(PriceContext key) {
        return key.chain() == BTC;
    }

    @Override
    public Optional<Collection<PriceWithContext>> get(PriceContext priceContext) throws ProviderException {
        throwIfUnsupported(priceContext);
        LocalDate date = priceContext.date();
        if (tooOld(date)) {
            return Optional.empty();
        }
        if (isRecentEnoughForOhlcData(date)) {
            return Optional.of(getFromOhlcData());
        }
        return Optional.of(getFromTrades(date));
    }

    private Collection<PriceWithContext> getFromTrades(LocalDate date) {
        Optional<Price> averagePriceForFirstTrades = getAveragePriceForFirstTrades(date);
        if (averagePriceForFirstTrades.isEmpty()) {
            return Set.of();
        }
        return Set.of(new PriceWithContext(averagePriceForFirstTrades.get(), toContext(date)));
    }

    private Collection<PriceWithContext> getFromOhlcData() {
        Optional<KrakenOhlcDataDto> ohlcData = getWithFeignClient(krakenClient::getOhlcData);
        if (ohlcData.isEmpty()) {
            return Set.of();
        }
        return ohlcData.get().getOhlcEntries().stream()
                .map(entry -> new PriceWithContext(entry.getOpenPrice(), toContext(entry.getTimestamp())))
                .collect(Collectors.toList());
    }

    private PriceContext toContext(long timestamp) {
        return toContext(toLocalDate(timestamp));
    }

    private PriceContext toContext(LocalDate date) {
        return new PriceContext(date, BTC);
    }

    private Optional<Price> getAveragePriceForFirstTrades(LocalDate date) {
        long startEpochSeconds = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        Optional<KrakenTradesDto> prices = getWithFeignClient(() -> krakenClient.getTrades(startEpochSeconds));
        return prices.flatMap(krakenPricesDto -> getPriceAverage(startEpochSeconds, krakenPricesDto));
    }

    private Optional<Price> getPriceAverage(long startEpochSeconds, KrakenTradesDto krakenPricesDto) {
        BigDecimal totalVolume = getValidTradesStream(krakenPricesDto, startEpochSeconds)
                .map(Trade::getVolume)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return getValidTradesStream(krakenPricesDto, startEpochSeconds)
                .map(trade -> trade.getPrice().getAsBigDecimal().multiply(trade.getVolume()))
                .reduce(BigDecimal::add)
                .map(bigDecimal -> bigDecimal.divide(totalVolume, RoundingMode.HALF_UP))
                .map(Price::of);
    }

    private Stream<Trade> getValidTradesStream(KrakenTradesDto krakenPricesDto, long startEpochSeconds) {
        return krakenPricesDto.getTrades().stream()
                .filter(trade -> trade.getTimestamp() >= startEpochSeconds)
                .filter(trade -> trade.getTimestamp() < startEpochSeconds + ONE_DAY_IN_SECONDS)
                .filter(trade -> trade.getVolume().compareTo(BigDecimal.ZERO) > 0);
    }

    private boolean tooOld(LocalDate date) {
        return date.isBefore(NO_PRICE_BEFORE);
    }

    private boolean isRecentEnoughForOhlcData(LocalDate date) {
        // Kraken provides 720 days of daily OHLC data
        LocalDate sevenHundredDaysAgo = now().minusDays(700);
        return date.isAfter(sevenHundredDaysAgo);
    }

    private LocalDate now() {
        return LocalDate.now(ZoneOffset.UTC);
    }

    private LocalDate toLocalDate(long timestamp) {
        return LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC).toLocalDate();
    }

    private <T> Optional<T> getWithFeignClient(Supplier<Optional<T>> supplier) {
        logger.debug("Contacting Kraken API");
        return supplier.get();
    }
}
