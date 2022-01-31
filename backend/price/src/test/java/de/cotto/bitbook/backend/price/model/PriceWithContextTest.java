package de.cotto.bitbook.backend.price.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BSV;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static org.assertj.core.api.Assertions.assertThat;

class PriceWithContextTest {

    private static final Price PRICE = Price.of(10);
    private static final LocalDate DATE = LocalDate.of(2020, 1, 2);
    public static final PriceContext PRICE_CONTEXT = new PriceContext(DATE, BTC);
    private static final PriceWithContext PRICE_WITH_CONTEXT = new PriceWithContext(PRICE, PRICE_CONTEXT);

    @Test
    void uses_predecessor_chain_for_dates_before_fork() {
        LocalDate preForkDate = LocalDate.of(2017, 7, 31);
        assertThat(new PriceContext(preForkDate, BCH)).isEqualTo(new PriceContext(preForkDate, BTC));
    }

    @Test
    void uses_predecessor_chain_for_dates_before_nested_fork() {
        LocalDate veryOldDate = LocalDate.of(2014, 12, 24);
        assertThat(new PriceContext(veryOldDate, BSV)).isEqualTo(new PriceContext(veryOldDate, BTC));
    }

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