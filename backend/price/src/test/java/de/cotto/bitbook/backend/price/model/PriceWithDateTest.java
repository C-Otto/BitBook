package de.cotto.bitbook.backend.price.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PriceWithDateTest {

    private static final Price PRICE = Price.of(10);
    private static final LocalDate DATE = LocalDate.of(2020, 1, 2);

    private final PriceWithDate priceWithDate = new PriceWithDate(PRICE, DATE);

    @Test
    void getPrice() {
        assertThat(priceWithDate.getPrice()).isEqualTo(PRICE);
    }

    @Test
    void getDate() {
        assertThat(priceWithDate.getDate()).isEqualTo(DATE);
    }

    @Test
    void testEquals() {
        EqualsVerifier.configure().suppress(Warning.NULL_FIELDS).forClass(PriceWithDate.class).usingGetClass().verify();
    }
}