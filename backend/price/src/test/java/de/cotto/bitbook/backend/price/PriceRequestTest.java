package de.cotto.bitbook.backend.price;

import de.cotto.bitbook.backend.price.model.PriceContext;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;

class PriceRequestTest {
    private static final LocalDate DATE = LocalDate.of(2021, 1, 2);
    public static final PriceContext PRICE_CONTEXT = new PriceContext(DATE, BTC);

    @Test
    void getPriceContext() {
        assertThat(PriceRequest.createWithStandardPriority(PRICE_CONTEXT).getPriceContext()).isEqualTo(PRICE_CONTEXT);
    }

    @Test
    void getPriority_standard() {
        assertThat(PriceRequest.createWithStandardPriority(PRICE_CONTEXT).getPriority()).isEqualTo(STANDARD);
    }

    @Test
    void getPriority_lowest() {
        assertThat(PriceRequest.createWithLowestPriority(PRICE_CONTEXT).getPriority()).isEqualTo(LOWEST);
    }

    @Test
    void forCurrentPrice_priority() {
        assertThat(PriceRequest.forCurrentPrice(BTC).getPriority()).isEqualTo(STANDARD);
    }

    @Test
    void forCurrentPrice_priceContext() {
        assertThat(PriceRequest.forCurrentPrice(BTC).getPriceContext())
                .isEqualTo(new PriceContext(LocalDate.now(ZoneOffset.UTC), BTC));
    }

    @Test
    void testToString() {
        assertThat(PriceRequest.createWithStandardPriority(PRICE_CONTEXT)).hasToString(
                "PriceRequest{" +
                "priceContext=" + PRICE_CONTEXT +
                ", priority=STANDARD" +
                "}");
    }
}