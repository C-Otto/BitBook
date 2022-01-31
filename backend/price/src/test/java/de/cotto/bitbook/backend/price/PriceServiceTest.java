package de.cotto.bitbook.backend.price;

import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceContext;
import de.cotto.bitbook.backend.price.model.PriceWithContext;
import de.cotto.bitbook.backend.request.PrioritizedRequestWithResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.cotto.bitbook.backend.model.Chain.BTC;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {
    private static final LocalDate DATE = LocalDate.of(2013, 11, 12);
    public static final PriceContext PRICE_CONTEXT = new PriceContext(DATE, BTC);
    private static final LocalDate DATE_2 = LocalDate.of(2015, 2, 3);

    @InjectMocks
    private PriceService priceService;

    @Mock
    private PriceDao priceDao;

    @Mock
    private PrioritizingPriceProvider prioritizingPriceProvider;

    @Test
    void getPrice() {
        Price expectedPrice = mockPrice();
        Price price = priceService.getPrice(DATE.atTime(12, 13), BTC);
        assertThat(price).isEqualTo(expectedPrice);
    }

    @Test
    void getCurrentPrice() {
        Price expectedPrice = Price.of(123);
        PriceWithContext priceWithContext =
                new PriceWithContext(expectedPrice, new PriceContext(LocalDate.now(ZoneOffset.UTC), BTC));
        mockResult(PriceRequest.forCurrentPrice(BTC), Set.of(priceWithContext));
        assertThat(priceService.getCurrentPrice(BTC)).isEqualTo(Price.of(123));
    }

    @Test
    void getPrice_failure() {
        PriceRequest request = PriceRequest.createWithStandardPriority(new PriceContext(DATE, BTC));
        when(prioritizingPriceProvider.getPrices(argThatMatches(request)))
                .then(invocation -> {
                    PriceRequest priceRequest = invocation.getArgument(0);
                    PrioritizedRequestWithResult<PriceContext, Collection<PriceWithContext>> future =
                            priceRequest.getWithResultFuture();
                    future.stopWithoutResult();
                    return future;
                });
        Price price = priceService.getPrice(DATE.atStartOfDay(), BTC);
        assertThat(price).isEqualTo(Price.UNKNOWN);
    }

    @Test
    void getPrice_persists_prices_from_priceProvider() {
        PriceContext priceContext = new PriceContext(DATE, BTC);
        Set<PriceWithContext> prices = Set.of(new PriceWithContext(Price.of(10), priceContext));
        mockResult(PriceRequest.createWithStandardPriority(priceContext), prices);

        priceService.getPrice(DATE.atStartOfDay(), BTC);

        verify(priceDao).savePrices(prices);
    }

    @Test
    void getPrice_returns_persisted_price() {
        Price expectedPrice = Price.of(10);
        when(priceDao.getPrice(PRICE_CONTEXT)).thenReturn(Optional.of(expectedPrice));

        Price price = priceService.getPrice(DATE.atStartOfDay(), BTC);

        verify(prioritizingPriceProvider, never()).getPrices(any());
        assertThat(price).isEqualTo(expectedPrice);
    }

    @Test
    void getPrice_persists_other_prices() {
        Price price1 = Price.of(1);
        Price price2 = Price.of(2);
        Price price3 = Price.of(3);
        LocalDate date1 = LocalDate.of(2005, 1, 1);
        LocalDate date2 = LocalDate.of(2006, 1, 1);
        LocalDate date3 = LocalDate.of(2007, 1, 1);
        Set<PriceWithContext> prices = Set.of(
                new PriceWithContext(price1, new PriceContext(date1, BTC)),
                new PriceWithContext(price2, new PriceContext(date2, BTC)),
                new PriceWithContext(price3, new PriceContext(date3, BTC))
        );
        mockResult(PriceRequest.createWithStandardPriority(new PriceContext(date2, BTC)), prices);

        priceService.getPrice(date2.atStartOfDay(), BTC);

        verify(priceDao).savePrices(prices);
    }

    @Test
    void getPrices() {
        PriceContext context2 = new PriceContext(DATE_2, BTC);
        mockResult(
                PriceRequest.createWithStandardPriority(PRICE_CONTEXT),
                Set.of(new PriceWithContext(Price.of(1), PRICE_CONTEXT))
        );
        mockResult(
                PriceRequest.createWithStandardPriority(context2),
                Set.of(new PriceWithContext(Price.of(2), context2))
        );

        Set<LocalDateTime> dateTimes = Set.of(DATE.atTime(2, 3), DATE_2.atTime(4, 5));
        Map<PriceContext, Price> prices = priceService.getPrices(dateTimes, BTC);

        assertThat(prices).containsExactlyInAnyOrderEntriesOf(Map.of(
                PRICE_CONTEXT, Price.of(1),
                context2, Price.of(2)
        ));
    }

    @Test
    void getPrices_persists_additional_results_without_returning_them() {
        Set<PriceWithContext> returnedResults = Set.of(
                new PriceWithContext(Price.of(1), new PriceContext(DATE.minusDays(1), BTC)),
                new PriceWithContext(Price.of(2), PRICE_CONTEXT),
                new PriceWithContext(Price.of(3), new PriceContext(DATE.plusDays(1), BTC))
        );
        mockResult(PriceRequest.createWithStandardPriority(PRICE_CONTEXT),
                returnedResults
        );

        Set<LocalDateTime> dateTimes = Set.of(DATE.atTime(2, 3));
        Map<PriceContext, Price> prices = priceService.getPrices(dateTimes, BTC);

        assertThat(prices).containsExactly(entry(PRICE_CONTEXT, Price.of(2)));
        verify(priceDao).savePrices(returnedResults);
    }

    @Test
    void getPrices_same_date_different_time() {
        mockResult(
                PriceRequest.createWithStandardPriority(PRICE_CONTEXT),
                Set.of(new PriceWithContext(Price.of(1), PRICE_CONTEXT))
        );

        Set<LocalDateTime> dateTimes = Set.of(DATE.atTime(2, 3), DATE.atTime(4, 5));
        Map<PriceContext, Price> prices = priceService.getPrices(dateTimes, BTC);

        assertThat(prices).containsExactly(entry(PRICE_CONTEXT, Price.of(1)));
    }

    @Test
    void requestPriceInBackground_with_lowest_priority() {
        PriceRequest request = PriceRequest.createWithLowestPriority(PRICE_CONTEXT);
        mockResult(request, Set.of());

        priceService.requestPriceInBackground(DATE.atTime(23, 12), BTC);

        verify(prioritizingPriceProvider).getPrices(argThatMatches(request));
    }

    @Test
    void requestPriceInBackground_with_lowest_priority_persists_results() {
        PriceRequest request = PriceRequest.createWithLowestPriority(PRICE_CONTEXT);
        Set<PriceWithContext> pricesWithContexts = Set.of(new PriceWithContext(Price.of(100), PRICE_CONTEXT));
        mockResult(request, pricesWithContexts);

        priceService.requestPriceInBackground(DATE.atTime(23, 12), BTC);

        verify(priceDao).savePrices(pricesWithContexts);
    }

    private Price mockPrice() {
        Price expectedPrice = Price.of(10);
        mockResult(PriceRequest.createWithStandardPriority(PRICE_CONTEXT), Set.of(
                new PriceWithContext(expectedPrice.add(Price.of(1_000_000)), new PriceContext(DATE.minusDays(1), BTC)),
                new PriceWithContext(expectedPrice, PRICE_CONTEXT),
                new PriceWithContext(expectedPrice.add(Price.of(1_000_000)), new PriceContext(DATE.plusDays(1), BTC))
        ));
        return expectedPrice;
    }

    private void mockResult(PriceRequest expectedRequest, Set<PriceWithContext> result) {
        when(prioritizingPriceProvider.getPrices(argThatMatches(expectedRequest)))
                .then(invocation -> {
                    PriceRequest priceRequest = invocation.getArgument(0);
                    PrioritizedRequestWithResult<PriceContext, Collection<PriceWithContext>> future =
                            priceRequest.getWithResultFuture();
                    future.provideResult(result);
                    return future;
                });
    }

    private PriceRequest argThatMatches(PriceRequest expectedRequest) {
        return ArgumentMatchers.argThat(request -> {
            if (request == null) {
                return false;
            }
            boolean sameContext = request.getPriceContext().equals(expectedRequest.getPriceContext());
            boolean samePriority = request.getPriority().equals(expectedRequest.getPriority());
            return sameContext && samePriority;
        });
    }
}
