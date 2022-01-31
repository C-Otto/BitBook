package de.cotto.bitbook.backend.price.kraken;

import de.cotto.bitbook.backend.ProviderException;
import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceContext;
import de.cotto.bitbook.backend.price.model.PriceWithContext;
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

import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
    void getName() {
        assertThat(krakenPriceProvider.getName()).isEqualTo("KrakenPriceProvider");
    }

    @Test
    void isSupported_btc() {
        assertThat(krakenPriceProvider.isSupported(new PriceContext(today(), BTC))).isTrue();
    }

    @Test
    void isSupported_bch() {
        assertThat(krakenPriceProvider.isSupported(new PriceContext(today(), BCH))).isFalse();
    }

    @Test
    void get_unsupported_chain() {
        PriceContext priceContext = new PriceContext(today(), BCH);
        assertThatExceptionOfType(ProviderException.class).isThrownBy(() -> krakenPriceProvider.get(priceContext));
    }

    @Test
    void current_price() throws Exception {
        Price expectedPrice = Price.of(35_000);
        PriceContext priceContext = new PriceContext(today(), BTC);

        returnMockedOhlcData(expectedPrice, today());
        Optional<Collection<PriceWithContext>> prices = krakenPriceProvider.get(priceContext);

        Collection<PriceWithContext> priceWithContextCollection = prices.orElseThrow();
        assertThat(priceWithContextCollection).usingRecursiveFieldByFieldElementComparator()
                .contains(new PriceWithContext(expectedPrice, priceContext));
        verify(krakenClient, never()).getTrades(anyLong());
    }

    @Test
    void getYesterdaysPrice() throws Exception {
        Price expectedPrice = Price.of(30_000);
        LocalDate yesterday = today().minusDays(1);
        PriceContext priceContext = new PriceContext(yesterday, BTC);

        returnMockedOhlcData(expectedPrice, yesterday);
        Optional<Collection<PriceWithContext>> prices = krakenPriceProvider.get(priceContext);

        Collection<PriceWithContext> priceWithContextCollection = prices.orElseThrow();
        assertThat(priceWithContextCollection).usingRecursiveFieldByFieldElementComparator()
                .contains(new PriceWithContext(expectedPrice, priceContext));
        assertThat(priceWithContextCollection).hasSize(3);
        verify(krakenClient, never()).getTrades(anyLong());
    }

    @Test
    void getYesterdaysPrice_no_data() throws Exception {
        LocalDate yesterday = today().minusDays(1);

        when(krakenClient.getOhlcData()).thenReturn(Optional.empty());

        Optional<Collection<PriceWithContext>> prices = krakenPriceProvider.get(new PriceContext(yesterday, BTC));

        assertThat(prices.orElseThrow()).isEmpty();
        verify(krakenClient, never()).getTrades(anyLong());
    }

    @Test
    void getPriceTwoYearsAgo() throws Exception {
        Price expectedPrice = Price.of(20_000);
        LocalDate twoYearsAgo = today().minusYears(2);
        PriceContext priceContext = new PriceContext(twoYearsAgo, BTC);

        long expectedSinceEpochSeconds = twoYearsAgo.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        Optional<KrakenTradesDto> mockedTrades = mockTradesWithAverage(expectedPrice, expectedSinceEpochSeconds);
        when(krakenClient.getTrades(anyLong())).thenReturn(mockedTrades);

        Optional<Collection<PriceWithContext>> prices = krakenPriceProvider.get(priceContext);

        verify(krakenClient).getTrades(expectedSinceEpochSeconds);
        assertThat(prices).isNotEmpty();
        assertThat(prices.get()).usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new PriceWithContext(expectedPrice, priceContext));
    }

    @Test
    void getPriceTwoYearsAgo_no_trade() throws Exception {
        LocalDate twoYearsAgo = today().minusYears(2);
        long expectedSinceEpochSeconds = twoYearsAgo.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        when(krakenClient.getTrades(anyLong())).thenReturn(noTrades());

        Optional<Collection<PriceWithContext>> prices = krakenPriceProvider.get(new PriceContext(twoYearsAgo, BTC));

        verify(krakenClient).getTrades(expectedSinceEpochSeconds);
        assertThat(prices).contains(Set.of());
    }

    @Test
    void getPriceTwoYearsAgo_only_zero_volume_trade() throws Exception {
        LocalDate twoYearsAgo = today().minusYears(2);
        long expectedSinceEpochSeconds = twoYearsAgo.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        Optional<KrakenTradesDto> mockedTrades = mockTradesWithZeroVolume(expectedSinceEpochSeconds);
        when(krakenClient.getTrades(anyLong())).thenReturn(mockedTrades);

        Optional<Collection<PriceWithContext>> prices = krakenPriceProvider.get(new PriceContext(twoYearsAgo, BTC));
        assertThat(prices).contains(Set.of());
    }

    @Test
    void getPriceUnknown() throws Exception {
        LocalDate wayTooEarly = today().minusYears(20);

        Optional<Collection<PriceWithContext>> prices = krakenPriceProvider.get(new PriceContext(wayTooEarly, BTC));

        verify(krakenClient, never()).getTrades(anyLong());
        assertThat(prices).isEmpty();
    }

    private Optional<KrakenTradesDto> mockTradesWithAverage(Price averagePrice, long start) {
        KrakenTradesDto krakenTradesDto = mock(KrakenTradesDto.class);
        Price oneMillion = Price.of(1_000_000);
        KrakenTradesDto.Trade earlyTrade = mockTrade(oneMillion, BigDecimal.valueOf(100.0), start - 1);
        KrakenTradesDto.Trade trade1 = mockTrade(averagePrice.add(Price.of(10)), BigDecimal.ONE, start);
        KrakenTradesDto.Trade trade2 = mockTrade(averagePrice, BigDecimal.TEN, start + 1);
        KrakenTradesDto.Trade tradeZeroVolume = mockTrade(oneMillion, BigDecimal.ZERO, start + 1);
        KrakenTradesDto.Trade trade3 =
                mockTrade(averagePrice.subtract(Price.of(5)), BigDecimal.valueOf(2), start + 2);
        KrakenTradesDto.Trade lateTrade = mockTrade(oneMillion, BigDecimal.TEN, start + ONE_DAY_IN_SECONDS);
        when(krakenTradesDto.getTrades())
                .thenReturn(List.of(earlyTrade, trade1, trade2, trade3, tradeZeroVolume, lateTrade));
        return Optional.of(krakenTradesDto);
    }

    private Optional<KrakenTradesDto> mockTradesWithZeroVolume(long start) {
        KrakenTradesDto krakenTradesDto = mock(KrakenTradesDto.class);
        KrakenTradesDto.Trade tradeZeroVolume = mockTrade(Price.of(1), BigDecimal.ZERO, start);
        when(krakenTradesDto.getTrades()).thenReturn(List.of(tradeZeroVolume));
        return Optional.of(krakenTradesDto);
    }

    private KrakenTradesDto.Trade mockTrade(Price price, BigDecimal volume, long timestamp) {
        KrakenTradesDto.Trade trade = mock(KrakenTradesDto.Trade.class);
        when(trade.getPrice()).thenReturn(price);
        when(trade.getVolume()).thenReturn(volume);
        when(trade.getTimestamp()).thenReturn(timestamp);
        return trade;
    }

    private void returnMockedOhlcData(Price expectedPrice, LocalDate today) {
        Optional<KrakenOhlcDataDto> mockedOhlcData = mockOhlcData(expectedPrice, today);
        when(krakenClient.getOhlcData()).thenReturn(mockedOhlcData);
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

    private LocalDate today() {
        return LocalDate.now(ZoneOffset.UTC);
    }
}