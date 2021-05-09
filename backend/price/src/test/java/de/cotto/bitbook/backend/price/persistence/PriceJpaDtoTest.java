package de.cotto.bitbook.backend.price.persistence;

import de.cotto.bitbook.backend.price.model.Price;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PriceJpaDtoTest {
    @Test
    void toModel() {
        Price price = Price.of(123);
        PriceJpaDto dto = new PriceJpaDto();
        dto.setAsBigDecimal(price.getAsBigDecimal());

        assertThat(dto.toModel()).isEqualTo(price);
    }

    @Test
    void fromModel() {
        Price model = Price.of(500);
        PriceJpaDto expected = new PriceJpaDto();
        expected.setAsBigDecimal(model.getAsBigDecimal());

        assertThat(PriceJpaDto.fromModel(model)).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void testToString() {
        PriceJpaDto dto = new PriceJpaDto();
        dto.setAsBigDecimal(BigDecimal.ONE);
        assertThat(dto).hasToString("PriceJpaDto{asBigDecimal=1}");
    }
}