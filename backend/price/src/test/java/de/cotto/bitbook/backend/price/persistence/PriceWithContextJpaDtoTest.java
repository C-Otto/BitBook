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
    private static final LocalDate DATE = LocalDate.of(1983, 9, 24);
    public static final PriceContext PRICE_CONTEXT_BCH = new PriceContext(DATE, BCH);
    private static final PriceContext PRICE_CONTEXT_BTC = new PriceContext(DATE, BTC);
    private static final Price PRICE = Price.of(500);

    @Test
    void toModel() {
        PriceWithContextJpaDto dto = createDto("BTC");
        PriceWithContext expected = new PriceWithContext(PRICE, PRICE_CONTEXT_BTC);
        assertThat(dto.toModel()).isEqualTo(expected);
    }

    @Test
    void toModel_bch() {
        PriceWithContextJpaDto dto = createDto("BCH");
        PriceWithContext expected = new PriceWithContext(PRICE, PRICE_CONTEXT_BCH);
        assertThat(dto.toModel()).isEqualTo(expected);
    }

    @Test
    void fromModel() {
        PriceWithContext model = new PriceWithContext(PRICE, PRICE_CONTEXT_BTC);
        PriceWithContextJpaDto expected = createDto("BTC");

        assertThat(PriceWithContextJpaDto.fromModel(model)).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void fromModel_bch() {
        PriceWithContext model = new PriceWithContext(PRICE, PRICE_CONTEXT_BCH);
        PriceWithContextJpaDto expected = createDto("BCH");

        assertThat(PriceWithContextJpaDto.fromModel(model)).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void testToString() {
        assertThat(createDto("BTC")).hasToString(
                "PriceWithDateJpaDto{" +
                "price=PriceJpaDto{asBigDecimal=500.00000000}" +
                ", date=1983-09-24" +
                ", chain=BTC" +
                "}"
        );
    }

    private PriceWithContextJpaDto createDto(String chain) {
        PriceWithContextJpaDto dto = new PriceWithContextJpaDto();
        PriceJpaDto priceJpaDto = new PriceJpaDto();
        priceJpaDto.setAsBigDecimal(PRICE.getAsBigDecimal());
        dto.setPrice(priceJpaDto);
        dto.setChain(chain);
        dto.setDate(DATE);
        return dto;
    }
}