package de.cotto.bitbook.backend.price.persistence;

import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.price.model.PriceWithDate;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PriceWithDateJpaDtoTest {
    private static final LocalDate DATE = LocalDate.of(1983, 9, 24);
    private static final Price PRICE = Price.of(500);

    @Test
    void toModel() {
        PriceWithDateJpaDto dto = createDto();
        PriceWithDate expected = new PriceWithDate(PRICE, DATE);
        assertThat(dto.toModel()).isEqualTo(expected);
    }

    @Test
    void fromModel() {
        PriceWithDate model = new PriceWithDate(PRICE, DATE);
        PriceWithDateJpaDto expected = createDto();

        assertThat(PriceWithDateJpaDto.fromModel(model)).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void testToString() {
        assertThat(createDto()).hasToString(
                "PriceWithDateJpaDto{" +
                "price=PriceJpaDto{asBigDecimal=500.00000000}" +
                ", date=1983-09-24" +
                "}"
        );
    }

    private PriceWithDateJpaDto createDto() {
        PriceWithDateJpaDto dto = new PriceWithDateJpaDto();
        PriceJpaDto priceJpaDto = new PriceJpaDto();
        priceJpaDto.setAsBigDecimal(PRICE.getAsBigDecimal());
        dto.setPrice(priceJpaDto);
        dto.setDate(DATE);
        return dto;
    }
}