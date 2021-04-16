package de.cotto.bitbook.backend.transaction.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class CoinsTest {

    private static final Coins ONE_COIN = Coins.ofSatoshis(100_000_000);

    @Test
    void add() {
        assertThat(Coins.ofSatoshis(123).add(Coins.ofSatoshis(456))).isEqualTo(Coins.ofSatoshis(123 + 456));
    }

    @Test
    void subtract() {
        assertThat(Coins.ofSatoshis(456).subtract(Coins.ofSatoshis(123))).isEqualTo(Coins.ofSatoshis(456 - 123));
    }

    @Test
    void absolute_for_positive() {
        assertThat(Coins.ofSatoshis(456).absolute()).isEqualTo(Coins.ofSatoshis(456));
    }

    @Test
    void absolute_for_negative() {
        assertThat(Coins.ofSatoshis(-456).absolute()).isEqualTo(Coins.ofSatoshis(456));
    }

    @Test
    void compareTo_greater_than() {
        assertThat(Coins.ofSatoshis(2).compareTo(Coins.ofSatoshis(1))).isGreaterThan(0);
    }

    @Test
    void compareTo_equal() {
        assertThat(Coins.ofSatoshis(0).compareTo(Coins.NONE)).isEqualTo(0);
    }

    @Test
    void compareTo_smaller_than() {
        assertThat(Coins.ofSatoshis(1).compareTo(Coins.ofSatoshis(2))).isLessThan(0);
    }

    @Test
    void isPositive() {
        assertThat(Coins.ofSatoshis(100).isPositive()).isTrue();
        assertThat(Coins.ofSatoshis(-100).isPositive()).isFalse();
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    void isNonPositive() {
        assertThat(Coins.ofSatoshis(-100).isNonPositive()).isTrue();
        assertThat(Coins.ofSatoshis(0).isNonPositive()).isTrue();
        assertThat(Coins.ofSatoshis(100).isNonPositive()).isFalse();
    }

    @Test
    void isNegative() {
        assertThat(Coins.ofSatoshis(-100).isNegative()).isTrue();
        assertThat(Coins.ofSatoshis(100).isNegative()).isFalse();
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    void isNonNegative() {
        assertThat(Coins.ofSatoshis(-100).isNonNegative()).isFalse();
        assertThat(Coins.ofSatoshis(0).isNonNegative()).isTrue();
        assertThat(Coins.ofSatoshis(100).isNonNegative()).isTrue();
    }

    @Test
    void isNegative_isPositive_zero_coins() {
        assertThat(Coins.NONE.isPositive()).isFalse();
        assertThat(Coins.NONE.isNegative()).isFalse();
    }

    @Test
    void none() {
        assertThat(Coins.NONE).isEqualTo(Coins.ofSatoshis(0));
    }

    @Test
    void ofSatoshi() {
        Coins coins = Coins.ofSatoshis(100_000_000);
        assertThat(coins).isEqualTo(ONE_COIN);
    }

    @Test
    void getSatoshis() {
        long satoshis = 1_234L;
        assertThat(Coins.ofSatoshis(satoshis).getSatoshis()).isEqualTo(satoshis);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Coins.class).usingGetClass().verify();
    }

    @Test
    void justSatoshi() {
        assertThat(Coins.ofSatoshis(12_345)).hasToString("   0.00012345");
    }

    @Test
    void justMilliCoins() {
        assertThat(Coins.ofSatoshis(12_300_000)).hasToString("   0.123     ");
    }

    @Test
    void formats_decimal_point_with_english_locale() {
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.GERMAN);
        try {
            assertThat(Coins.ofSatoshis(12_300_000)).hasToString("   0.123     ");
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    @Test
    void justCoins() {
        assertThat(Coins.ofSatoshis(12_300_000_000L)).hasToString(" 123         ");
    }

    @Test
    void everything() {
        assertThat(Coins.ofSatoshis(12_345_678_123L)).hasToString(" 123.45678123");
    }

    @Test
    void negative() {
        assertThat(Coins.ofSatoshis(-12_345_678_123L)).hasToString("-123.45678123");
    }

    @Test
    void mixed() {
        assertThat(Coins.ofSatoshis(12_000_678_100L)).hasToString(" 120.006781  ");
    }

    @Test
    void manyCoins() {
        assertThat(Coins.ofSatoshis(321_000_678_100L)).hasToString("3210.006781  ");
    }

    @Test
    void manyCoins_negative() {
        assertThat(Coins.ofSatoshis(-321_000_678_100L)).hasToString("-3210.006781  ");
    }
}