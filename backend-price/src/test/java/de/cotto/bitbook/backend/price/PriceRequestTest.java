package de.cotto.bitbook.backend.price;

import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceWithDate;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Set;

import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;

class PriceRequestTest {
    private static final LocalDate DATE = LocalDate.of(2021, 1, 2);

    @Nullable
    private Collection<PriceWithDate> seen;

    @Test
    void getDate() {
        assertThat(PriceRequest.forDateStandardPriority(DATE).getDate()).isEqualTo(DATE);
    }

    @Test
    void getPriority_standard() {
        assertThat(PriceRequest.forDateStandardPriority(DATE).getPriority()).isEqualTo(STANDARD);
    }

    @Test
    void getPriority_lowest() {
        assertThat(PriceRequest.forDateLowestPriority(DATE).getPriority()).isEqualTo(LOWEST);
    }

    @Test
    void forCurrentPrice_priority() {
        assertThat(PriceRequest.forCurrentPrice().getPriority()).isEqualTo(STANDARD);
    }

    @Test
    void forCurrentPrice_date() {
        assertThat(PriceRequest.forCurrentPrice().getDate()).isEqualTo(LocalDate.now(ZoneOffset.UTC));
    }

    @Test
    void getWithResultConsumer() {
        Set<PriceWithDate> result = Set.of(new PriceWithDate(Price.of(10), DATE));

        PriceRequest request = PriceRequest.forDateLowestPriority(DATE);

        PriceRequest requestWithResultConsumer = request.getWithResultConsumer(this::resultConsumer);
        requestWithResultConsumer.getWithResultFuture().provideResult(result);

        assertThat(requestWithResultConsumer.getDate()).isEqualTo(DATE);
        assertThat(seen).isEqualTo(result);
    }

    private void resultConsumer(Collection<PriceWithDate> result) {
        seen = result;
    }

    @Test
    void testToString() {
        assertThat(PriceRequest.forDateStandardPriority(DATE)).hasToString(
                "PriceRequest{" +
                "date=2021-01-02" +
                ", priority=STANDARD" +
                "}");
    }
}