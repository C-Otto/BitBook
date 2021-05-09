package de.cotto.bitbook.backend.price.persistence;

import com.google.common.annotations.VisibleForTesting;
import de.cotto.bitbook.backend.price.model.Price;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
class PriceJpaDto {
    private static final int SCALE = 8;

    @Nullable
    @Column(name = "price", precision = 16, scale = SCALE)
    private BigDecimal asBigDecimal;

    PriceJpaDto() {
        // for JPA
    }

    protected static PriceJpaDto fromModel(Price price) {
        PriceJpaDto dto = new PriceJpaDto();
        dto.asBigDecimal = price.getAsBigDecimal();
        return dto;
    }

    protected Price toModel() {
        return Price.of(Objects.requireNonNull(asBigDecimal));
    }

    @VisibleForTesting
    protected void setAsBigDecimal(@Nonnull BigDecimal asBigDecimal) {
        this.asBigDecimal = asBigDecimal;
    }

    @Override
    public String toString() {
        return "PriceJpaDto{" +
               "asBigDecimal=" + asBigDecimal +
               '}';
    }
}
