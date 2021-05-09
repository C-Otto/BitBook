package de.cotto.bitbook.backend.price;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;

class PriceRequestTest {
    private static final LocalDate DATE = LocalDate.of(2021, 1, 2);

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
    void testToString() {
        assertThat(PriceRequest.forDateStandardPriority(DATE)).hasToString(
                "PriceRequest{" +
                "date=2021-01-02" +
                ", priority=STANDARD" +
                "}");
    }
}