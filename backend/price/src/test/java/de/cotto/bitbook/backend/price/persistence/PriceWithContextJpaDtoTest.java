package de.cotto.bitbook.backend.price.persistence;

import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceContext;
import de.cotto.bitbook.backend.price.model.PriceWithContext;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static org.assertj.core.api.Assertions.assertThat;

class PriceWithContextJpaDtoTest {
    private static final LocalDate DATE = LocalDate.of(2009, 1, 3);
    private static final LocalDate DATE_BCH = LocalDate.of(2017, 8, 1);
    private static final PriceContext PRICE_CONTEXT_BTC = new PriceContext(DATE, BTC);
    public static final PriceContext PRICE_CONTEXT_BCH = new PriceContext(DATE_BCH, BCH);
    private static final Price PRICE = Price.of(500);

    @Test
    void toModel() {
        PriceWithContextJpaDto dto = createDto(DATE, "BTC");
        PriceWithContext expected = new PriceWithContext(PRICE, PRICE_CONTEXT_BTC);
        assertThat(dto.toModel()).isEqualTo(expected);
    }

    @Test
    void toModel_bch() {
        PriceWithContextJpaDto dto = createDto(DATE_BCH, "BCH");
        PriceWithContext expected = new PriceWithContext(PRICE, PRICE_CONTEXT_BCH);
        assertThat(dto.toModel()).isEqualTo(expected);
    }

    @Test
    void fromModel() {
        PriceWithContext model = new PriceWithContext(PRICE, PRICE_CONTEXT_BTC);
        PriceWithContextJpaDto expected = createDto(DATE, "BTC");

        assertThat(PriceWithContextJpaDto.fromModel(model)).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void fromModel_bch() {
        PriceWithContext model = new PriceWithContext(PRICE, PRICE_CONTEXT_BCH);
        PriceWithContextJpaDto expected = createDto(DATE_BCH, "BCH");

        assertThat(PriceWithContextJpaDto.fromModel(model)).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void testToString() {
        assertThat(createDto(DATE, "BTC")).hasToString(
                "PriceWithDateJpaDto{" +
                "price=PriceJpaDto{asBigDecimal=500.00000000}" +
                ", date=2009-01-03" +
                ", chain=BTC" +
                "}"
        );
    }

    private PriceWithContextJpaDto createDto(LocalDate date, String chain) {
        PriceWithContextJpaDto dto = new PriceWithContextJpaDto();
        PriceJpaDto priceJpaDto = new PriceJpaDto();
        priceJpaDto.setAsBigDecimal(PRICE.getAsBigDecimal());
        dto.setPrice(priceJpaDto);
        dto.setChain(chain);
        dto.setDate(date);
        return dto;
    }
}