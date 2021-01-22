package de.cotto.bitbook.backend.price.kraken;

import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceWithDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KrakenPriceProviderTest {
    private static final long ONE_DAY_IN_SECONDS = 24 * 60 * 60;

    @InjectMocks
    private KrakenPriceProvider krakenPriceProvider;

    @Mock
    private KrakenClient krakenClient;

    @Test
    void current_price() {
        Price expectedPrice = Price.of(35_000);
        LocalDate today = now();

        Optional<KrakenOhlcDataDto> mockedOhlcData = mockOhlcData(expectedPrice, today);
        when(krakenClient.getOhlcData()).thenReturn(mockedOhlcData);
        Optional<Collection<PriceWithDate>> prices = krakenPriceProvider.get(today);

        Collection<PriceWithDate> priceWithDateCollection = prices.orElseThrow();
        assertThat(priceWithDateCollection).usingFieldByFieldElementComparator()
                .contains(new PriceWithDate(expectedPrice, today));
        verify(krakenClient, never()).getTrades(anyLong());
    }

    @Test
    void getYesterdaysPrice() {
        Price expectedPrice = Price.of(30_000);
        LocalDate yesterday = now().minusDays(1);

        Optional<KrakenOhlcDataDto> mockedOhlcData = mockOhlcData(expectedPrice, yesterday);
        when(krakenClient.getOhlcData()).thenReturn(mockedOhlcData);
        Optional<Collection<PriceWithDate>> prices = krakenPriceProvider.get(yesterday);

        Collection<PriceWithDate> priceWithDateCollection = prices.orElseThrow();
        assertThat(priceWithDateCollection).usingFieldByFieldElementComparator()
                .contains(new PriceWithDate(expectedPrice, yesterday));
        assertThat(priceWithDateCollection).hasSize(3);
        verify(krakenClient, never()).getTrades(anyLong());
    }

    @Test
    void getYesterdaysPrice_no_data() {
        LocalDate yesterday = now().minusDays(1);

        when(krakenClient.getOhlcData()).thenReturn(Optional.empty());

        Optional<Collection<PriceWithDate>> prices = krakenPriceProvider.get(yesterday);

        assertThat(prices.orElseThrow()).isEmpty();
        verify(krakenClient, never()).getTrades(anyLong());
    }

    @Test
    void getPriceTwoYearsAgo() {
        Price expectedPrice = Price.of(20_000);
        LocalDate twoYearsAgo = now().minusYears(2);
        long expectedSinceEpochSeconds = twoYearsAgo.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        Optional<KrakenTradesDto> mockedTrades = mockTradesWithAverage(expectedPrice, expectedSinceEpochSeconds);
        when(krakenClient.getTrades(anyLong())).thenReturn(mockedTrades);

        Optional<Collection<PriceWithDate>> prices = krakenPriceProvider.get(twoYearsAgo);

        verify(krakenClient).getTrades(expectedSinceEpochSeconds);
        assertThat(prices).isNotEmpty();
        assertThat(prices.get()).usingFieldByFieldElementComparator()
                .containsExactly(new PriceWithDate(expectedPrice, twoYearsAgo));
    }

    @Test
    void getPriceTwoYearsAgo_no_trade() {
        LocalDate twoYearsAgo = now().minusYears(2);
        long expectedSinceEpochSeconds = twoYearsAgo.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        when(krakenClient.getTrades(anyLong())).thenReturn(noTrades());

        Optional<Collection<PriceWithDate>> prices = krakenPriceProvider.get(twoYearsAgo);

        verify(krakenClient).getTrades(expectedSinceEpochSeconds);
        assertThat(prices).contains(Set.of());
    }

    @Test
    void getPriceUnknown() {
        LocalDate wayTooEarly = now().minusYears(20);

        Optional<Collection<PriceWithDate>> prices = krakenPriceProvider.get(wayTooEarly);

        verify(krakenClient, never()).getTrades(anyLong());
        assertThat(prices).isEmpty();
    }

    private Optional<KrakenTradesDto> mockTradesWithAverage(Price averagePrice, long start) {
        KrakenTradesDto krakenTradesDto = mock(KrakenTradesDto.class);
        Price oneMillion = Price.of(1_000_000);
        KrakenTradesDto.Trade earlyTrade = mockTrade(oneMillion, BigDecimal.valueOf(100.0), start - 1);
        KrakenTradesDto.Trade trade1 = mockTrade(averagePrice, BigDecimal.TEN, start + 1);
        KrakenTradesDto.Trade trade2 = mockTrade(averagePrice.add(Price.of(10)), BigDecimal.ONE, start + 1);
        KrakenTradesDto.Trade tradeZeroVolume = mockTrade(averagePrice.add(Price.of(10)), BigDecimal.ZERO, start + 1);
        KrakenTradesDto.Trade trade3 =
                mockTrade(averagePrice.subtract(Price.of(5)), BigDecimal.valueOf(2), start + 1);
        KrakenTradesDto.Trade lateTrade = mockTrade(oneMillion, BigDecimal.TEN, start + ONE_DAY_IN_SECONDS);
        when(krakenTradesDto.getTrades())
                .thenReturn(List.of(earlyTrade, trade1, trade2, trade3, tradeZeroVolume, lateTrade));
        return Optional.of(krakenTradesDto);
    }

    private KrakenTradesDto.Trade mockTrade(Price price, BigDecimal volume, long timestamp) {
        KrakenTradesDto.Trade trade = mock(KrakenTradesDto.Trade.class);
        when(trade.getPrice()).thenReturn(price);
        when(trade.getVolume()).thenReturn(volume);
        when(trade.getTimestamp()).thenReturn(timestamp);
        return trade;
    }

    private Optional<KrakenOhlcDataDto> mockOhlcData(Price openPrice, LocalDate date) {
        long timestamp = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        KrakenOhlcDataDto krakenOhlcDataDto = mock(KrakenOhlcDataDto.class);
        KrakenOhlcDataDto.OhlcEntry entry1 = mockOhlcEntry(openPrice, timestamp - ONE_DAY_IN_SECONDS);
        KrakenOhlcDataDto.OhlcEntry entry2 = mockOhlcEntry(openPrice, timestamp);
        KrakenOhlcDataDto.OhlcEntry entry3 = mockOhlcEntry(openPrice, timestamp + ONE_DAY_IN_SECONDS);
        when(krakenOhlcDataDto.getOhlcEntries()).thenReturn(List.of(entry1, entry2, entry3));
        return Optional.of(krakenOhlcDataDto);
    }

    private KrakenOhlcDataDto.OhlcEntry mockOhlcEntry(Price openPrice, long timestamp) {
        KrakenOhlcDataDto.OhlcEntry entry = mock(KrakenOhlcDataDto.OhlcEntry.class);
        when(entry.getOpenPrice()).thenReturn(openPrice);
        when(entry.getTimestamp()).thenReturn(timestamp);
        return entry;
    }

    private Optional<KrakenTradesDto> noTrades() {
        KrakenTradesDto krakenTradesDto = mock(KrakenTradesDto.class);
        return Optional.of(krakenTradesDto);
    }

    private LocalDate now() {
        return LocalDate.now(ZoneOffset.UTC);
    }
}