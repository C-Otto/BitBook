package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.price.model.Price;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PriceFormatterTest {
    private static final Price PRICE = Price.of(100);

    @InjectMocks
    private PriceFormatter priceFormatter;

    @Test
    void zero() {
        assertThat(priceFormatter.format(Coins.ofSatoshis(1), PRICE)).isEqualTo("         0.00€");
    }

    @Test
    void unknown() {
        assertThat(priceFormatter.format(Coins.ofSatoshis(1), Price.UNKNOWN)).isEqualTo(" Price unknown");
    }

    @Test
    void standard() {
        assertThat(priceFormatter.format(Coins.ofSatoshis(1_234_567), PRICE)).isEqualTo("         1.23€");
    }

    @Test
    void round_up() {
        assertThat(priceFormatter.format(Coins.ofSatoshis(1_235_567), PRICE)).isEqualTo("         1.24€");
    }

    @Test
    void negative() {
        assertThat(priceFormatter.format(Coins.ofSatoshis(-1_234_567), PRICE)).isEqualTo("        -1.23€");
    }

    @Test
    void long_at_limit() {
        assertThat(priceFormatter.format(Coins.ofSatoshis(12_345_678_912_345L), PRICE)).isEqualTo("12,345,678.91€");
    }

    @Test
    void long_at_limit_negative() {
        assertThat(priceFormatter.format(Coins.ofSatoshis(-1_234_567_891_234L), PRICE)).isEqualTo("-1,234,567.89€");
    }

    @Test
    void longer_than_limit() {
        assertThat(priceFormatter.format(Coins.ofSatoshis(12_345_678_901_234L), PRICE)).isEqualTo("12,345,678.90€");
    }

    @Test
    void longer_than_limit_negative() {
        assertThat(priceFormatter.format(Coins.ofSatoshis(-12_345_678_901_234L), PRICE)).isEqualTo("-12,345,678.90€");
    }
}
