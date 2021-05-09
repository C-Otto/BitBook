package de.cotto.bitbook.backend.price.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PriceTest {

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Price.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        assertThat(Price.of(12.34)).hasToString("Price{12.34000000}");
    }

    @Test
    void testToString_unknown() {
        assertThat(Price.UNKNOWN).hasToString("Price{UNKNOWN}");
    }

    @Test
    void add() {
        assertThat(Price.of(5).add(Price.of(10))).isEqualTo(Price.of(15));
    }

    @Test
    void add_unknown() {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(
                () -> Price.of(5).add(Price.UNKNOWN)
        );
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(
                () -> Price.UNKNOWN.add(Price.of(5))
        );
    }

    @Test
    void subtract() {
        assertThat(Price.of(15).subtract(Price.of(10))).isEqualTo(Price.of(5));
    }

    @Test
    void subtract_unknown() {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(
                () -> Price.of(5).subtract(Price.UNKNOWN)
        );
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(
                () -> Price.UNKNOWN.subtract(Price.of(5))
        );
    }

    @Test
    void of_long() {
        assertThat(Price.of(15L)).isEqualTo(Price.of(15));
    }

    @Test
    void of_double() {
        assertThat(Price.of(15d)).isEqualTo(Price.of(15));
    }

    @Test
    void of_BigDecimal() {
        assertThat(Price.of(BigDecimal.valueOf(15))).isEqualTo(Price.of(15));
    }

    @Test
    void getAsBigDecimal() {
        assertThat(Price.of(15).getAsBigDecimal())
                .isEqualTo(BigDecimal.valueOf(15).setScale(8, RoundingMode.UNNECESSARY));
    }

    @Test
    void getAsBigDecimal_unknown() {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(
                Price.UNKNOWN::getAsBigDecimal
        );
    }
}