package de.cotto.bitbook.backend.price.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static de.cotto.bitbook.backend.model.Chain.BTC;
import static org.assertj.core.api.Assertions.assertThat;

class PriceWithContextTest {

    private static final Price PRICE = Price.of(10);
    private static final LocalDate DATE = LocalDate.of(2020, 1, 2);
    public static final PriceContext PRICE_CONTEXT = new PriceContext(DATE, BTC);
    private static final PriceWithContext PRICE_WITH_CONTEXT = new PriceWithContext(PRICE, PRICE_CONTEXT);

    @Test
    void getPrice() {
        assertThat(PRICE_WITH_CONTEXT.getPrice()).isEqualTo(PRICE);
    }

    @Test
    void getDate() {
        assertThat(PRICE_WITH_CONTEXT.getPriceContext()).isEqualTo(PRICE_CONTEXT);
    }

    @Test
    void testEquals() {
        EqualsVerifier.configure()
                .suppress(Warning.NULL_FIELDS)
                .forClass(PriceWithContext.class)
                .usingGetClass()
                .verify();
    }
}