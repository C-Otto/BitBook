package de.cotto.bitbook.backend.price;

import de.cotto.bitbook.backend.price.kraken.KrakenClient;
import de.cotto.bitbook.backend.price.kraken.KrakenTradesDto;
import de.cotto.bitbook.backend.price.kraken.KrakenTradesDto.Trade;
import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceContext;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static de.cotto.bitbook.backend.model.Chain.BTC;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class PriceServiceIT {
    private static final Price PRICE = Price.of(1234);
    private static final LocalDate DATE = LocalDate.of(2014, 1, 1);
    private static final long DATE_IN_EPOCH_SECONDS = DATE.atStartOfDay(ZoneOffset.UTC).toEpochSecond();

    @Autowired
    private PriceService priceService;

    @Autowired
    private PriceDao priceDao;

    @MockBean
    private KrakenClient krakenClient;

    @Test
    void getPrice() {
        when(krakenClient.getTrades(DATE_IN_EPOCH_SECONDS)).thenReturn(Optional.of(trades(DATE)));

        Price price = priceService.getPrice(DATE.atStartOfDay(), BTC);

        verify(krakenClient).getTrades(DATE_IN_EPOCH_SECONDS);
        assertThat(price).isEqualTo(PRICE);
    }

    @Test
    void requestPriceInBackground_async() {
        int delay = 1_000;
        LocalDateTime dateTime = LocalDateTime.of(2017, 2, 3, 1, 2);
        when(krakenClient.getTrades(anyLong())).then((Answer<Optional<KrakenTradesDto>>) invocation -> {
            Thread.sleep(delay);
            return Optional.of(trades(dateTime.toLocalDate()));
        });
        await().atMost((long) (0.9 * delay), MILLISECONDS).untilAsserted(
                () -> priceService.requestPriceInBackground(dateTime, BTC)
        );
        await().atMost(4 * delay, MILLISECONDS).untilAsserted(() ->
                assertThat(priceDao.getPrice(new PriceContext(dateTime.toLocalDate(), BTC))).contains(PRICE)
        );
    }

    @Test
    void concurrent_requests_for_different_dates() {
        LocalDateTime dateTime1 = LocalDateTime.of(2017, 3, 4, 1, 2);
        long epochSeconds1 = dateTime1.toLocalDate().atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        when(krakenClient.getTrades(epochSeconds1)).then((Answer<Optional<KrakenTradesDto>>) invocation -> {
            Thread.sleep(1_000);
            return Optional.empty();
        });

        LocalDateTime dateTime2 = LocalDateTime.of(2017, 3, 5, 1, 2);
        long epochSeconds2 = dateTime2.toLocalDate().atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        when(krakenClient.getTrades(epochSeconds2)).thenReturn(Optional.of(trades(DATE)));

        priceService.requestPriceInBackground(dateTime1, BTC);
        assertTimeout(Duration.ofMillis(100), () -> priceService.requestPriceInBackground(dateTime2, BTC));
    }

    private KrakenTradesDto trades(LocalDate date) {
        return new KrakenTradesDto(List.of(
                trade(PRICE, date),
                trade(Price.of(10), DATE.plusDays(1))
        ));
    }

    private Trade trade(Price expectedPrice, LocalDate date) {
        long timestamp = date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        return new Trade(expectedPrice, BigDecimal.ONE, timestamp);
    }
}
