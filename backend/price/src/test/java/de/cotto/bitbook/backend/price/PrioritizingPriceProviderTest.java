package de.cotto.bitbook.backend.price;

import de.cotto.bitbook.backend.ProviderException;
import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceContext;
import de.cotto.bitbook.backend.price.model.PriceWithContext;
import de.cotto.bitbook.backend.request.ResultFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static de.cotto.bitbook.backend.model.Chain.BTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrioritizingPriceProviderTest {
    private static final LocalDate DATE = LocalDate.of(2021, 1, 2);
    private static final PriceContext PRICE_CONTEXT = new PriceContext(DATE, BTC);
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    private PriceProvider priceProvider;
    private PrioritizingPriceProvider prioritizingPriceProvider;

    @BeforeEach
    void setUp() {
        priceProvider = mock(PriceProvider.class);
        lenient().when(priceProvider.isSupported(any())).thenReturn(true);
        prioritizingPriceProvider = new PrioritizingPriceProvider(List.of(priceProvider));
    }

    @Test
    void getPrices() throws Exception {
        PriceWithContext expected = new PriceWithContext(Price.of(2), PRICE_CONTEXT);
        when(priceProvider.get(PRICE_CONTEXT)).thenReturn(Optional.of(Set.of(expected)));

        PriceRequest request = PriceRequest.createWithStandardPriority(PRICE_CONTEXT);
        ResultFuture<Collection<PriceWithContext>> resultFuture = prioritizingPriceProvider.getPrices(request);
        workOnRequestsInBackground();

        assertThat(resultFuture.getResult().orElseThrow()).contains(expected);
    }

    @Test
    void getPrices_failure() throws Exception {
        when(priceProvider.get(PRICE_CONTEXT)).thenReturn(Optional.empty());

        PriceRequest request = PriceRequest.createWithStandardPriority(PRICE_CONTEXT);
        ResultFuture<Collection<PriceWithContext>> resultFuture = prioritizingPriceProvider.getPrices(request);
        workOnRequestsInBackground();

        assertThat(resultFuture.getResult()).isEmpty();
    }

    @Test
    void getPrices_error() throws Exception {
        when(priceProvider.get(PRICE_CONTEXT)).thenThrow(ProviderException.class);

        PriceRequest request = PriceRequest.createWithStandardPriority(PRICE_CONTEXT);
        ResultFuture<Collection<PriceWithContext>> resultFuture = prioritizingPriceProvider.getPrices(request);
        workOnRequestsInBackground();

        assertThat(resultFuture.getResult()).isEmpty();
    }

    @Test
    void getProvidedResultName() {
        assertThat(prioritizingPriceProvider.getProvidedResultName()).isEqualTo("Price");
    }

    private void workOnRequestsInBackground() {
        executor.execute(() -> prioritizingPriceProvider.workOnRequests());
    }
}