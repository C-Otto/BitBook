package de.cotto.bitbook.backend.price;

import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceWithDate;
import de.cotto.bitbook.backend.request.PrioritizedRequestWithResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PriceServiceTest {
    private static final LocalDate DATE = LocalDate.of(2013, 11, 12);

    @InjectMocks
    private PriceService priceService;

    @Mock
    private PriceDao priceDao;

    @Mock
    private PrioritizingPriceProvider prioritizingPriceProvider;

    @Test
    void getPrice_request() {
        Price expectedPrice = mockPrice();
        Price price = priceService.getPrice(PriceRequest.forDateStandardPriority(DATE));
        assertThat(price).isEqualTo(expectedPrice);
    }

    @Test
    void getPrice_date_time() {
        Price expectedPrice = mockPrice();
        Price price = priceService.getPrice(DATE.atTime(12, 13));
        assertThat(price).isEqualTo(expectedPrice);
    }

    @Test
    void getCurrentPrice() {
        Price expectedPrice = Price.of(123);
        PriceWithDate priceWithDate = new PriceWithDate(expectedPrice, LocalDate.now(ZoneOffset.UTC));
        mockResult(PriceRequest.forCurrentPrice(), Set.of(priceWithDate));
        assertThat(priceService.getCurrentPrice()).isEqualTo(Price.of(123));
    }

    @Test
    void getPrice_failure() {
        PriceRequest request = PriceRequest.forDateStandardPriority(DATE);
        when(prioritizingPriceProvider.getPrices(argThatMatches(request)))
                .then(invocation -> {
                    PriceRequest priceRequest = invocation.getArgument(0);
                    PrioritizedRequestWithResult<LocalDate, Collection<PriceWithDate>> future =
                            priceRequest.getWithResultFuture();
                    future.stopWithoutResult();
                    return future;
                });
        Price price = priceService.getPrice(request);
        assertThat(price).isEqualTo(Price.UNKNOWN);
    }

    @Test
    void getPrice_persists_prices_from_priceProvider() {
        Set<PriceWithDate> prices = Set.of(new PriceWithDate(Price.of(10), DATE));
        mockResult(PriceRequest.forDateStandardPriority(DATE), prices);

        priceService.getPrice(PriceRequest.forDateStandardPriority(DATE));

        verify(priceDao).savePrices(prices);
    }

    @Test
    void getPrice_returns_persisted_price() {
        Price expectedPrice = Price.of(10);
        when(priceDao.getPrice(DATE)).thenReturn(Optional.of(expectedPrice));

        Price price = priceService.getPrice(PriceRequest.forDateStandardPriority(DATE));

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
        Set<PriceWithDate> prices = Set.of(
                new PriceWithDate(price1, date1),
                new PriceWithDate(price2, date2),
                new PriceWithDate(price3, date3)
        );
        mockResult(PriceRequest.forDateStandardPriority(date2), prices);

        priceService.getPrice(PriceRequest.forDateStandardPriority(date2));

        verify(priceDao).savePrices(prices);
    }

    @Test
    void requestPriceInBackground_with_lowest_priority() {
        PriceRequest request = PriceRequest.forDateLowestPriority(DATE);
        mockResult(request, Set.of());

        priceService.requestPriceInBackground(DATE.atTime(23, 12));

        verify(prioritizingPriceProvider).getPrices(argThatMatches(request));
    }

    @Test
    void requestPriceInBackground_with_lowest_priority_persists_results() {
        PriceRequest request = PriceRequest.forDateLowestPriority(DATE);
        Set<PriceWithDate> priceWithDates = Set.of(new PriceWithDate(Price.of(100), DATE));
        mockResult(request, priceWithDates);

        priceService.requestPriceInBackground(DATE.atTime(23, 12));

        verify(priceDao).savePrices(priceWithDates);
    }

    private Price mockPrice() {
        Price expectedPrice = Price.of(10);
        mockResult(PriceRequest.forDateStandardPriority(DATE), Set.of(new PriceWithDate(expectedPrice, DATE)));
        return expectedPrice;
    }

    private void mockResult(PriceRequest expectedRequest, Set<PriceWithDate> result) {
        when(prioritizingPriceProvider.getPrices(argThatMatches(expectedRequest)))
                .then(invocation -> {
                    PriceRequest priceRequest = invocation.getArgument(0);
                    PrioritizedRequestWithResult<LocalDate, Collection<PriceWithDate>> future =
                            priceRequest.getWithResultFuture();
                    future.provideResult(result);
                    return future;
                });
    }

    private PriceRequest argThatMatches(PriceRequest expectedRequest) {
        return ArgumentMatchers.argThat(request -> {
            boolean sameDate = request.getDate().equals(expectedRequest.getDate());
            boolean samePriority = request.getPriority().equals(expectedRequest.getPriority());
            return sameDate && samePriority;
        });
    }
}
