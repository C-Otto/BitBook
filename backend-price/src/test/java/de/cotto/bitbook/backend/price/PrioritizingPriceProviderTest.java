package de.cotto.bitbook.backend.price;

import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceWithDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrioritizingPriceProviderTest {
    private static final LocalDate DATE = LocalDate.of(2021, 1, 2);
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    private PriceProvider priceProvider;
    private PrioritizingPriceProvider prioritizingPriceProvider;

    @BeforeEach
    void setUp() {
        priceProvider = mock(PriceProvider.class);
        prioritizingPriceProvider = new PrioritizingPriceProvider(List.of(priceProvider));
    }

    @Test
    void getPrices() {
        PriceWithDate expected = new PriceWithDate(Price.of(2), DATE);
        when(priceProvider.get(DATE)).thenReturn(Optional.of(Set.of(expected)));
        workOnRequestsInBackground();

        PriceRequest request = PriceRequest.forDateStandardPriority(DATE);
        Collection<PriceWithDate> result = prioritizingPriceProvider.getPrices(request).getResult().orElseThrow();
        assertThat(result).contains(expected);
    }

    @Test
    void getPrices_failure() {
        when(priceProvider.get(DATE)).thenReturn(Optional.empty());
        workOnRequestsInBackground();

        PriceRequest request = PriceRequest.forDateStandardPriority(DATE);
        Optional<Collection<PriceWithDate>> result = prioritizingPriceProvider.getPrices(request).getResult();
        assertThat(result).isEmpty();
    }

    @Test
    void getProvidedResultName() {
        assertThat(prioritizingPriceProvider.getProvidedResultName()).isEqualTo("Price");
    }

    private void workOnRequestsInBackground() {
        executor.execute(() -> {
            await().atMost(2, SECONDS)
                    .until(() -> !prioritizingPriceProvider.getRequestQueue().isEmpty());
            prioritizingPriceProvider.workOnRequests();
        });
    }
}