package de.cotto.bitbook.backend.price.persistence;

import com.google.common.annotations.VisibleForTesting;
import de.cotto.bitbook.backend.price.model.PriceWithDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "prices")
class PriceWithDateJpaDto {
    @Nullable
    @Embedded
    private PriceJpaDto price;

    @Id
    @Nullable
    private LocalDate date;

    PriceWithDateJpaDto() {
        // for JPA
    }

    protected PriceWithDate toModel() {
        return new PriceWithDate(Objects.requireNonNull(price).toModel(), Objects.requireNonNull(date));
    }

    protected static PriceWithDateJpaDto fromModel(PriceWithDate priceWithDate) {
        PriceWithDateJpaDto dto = new PriceWithDateJpaDto();
        dto.date = priceWithDate.getDate();
        dto.price = PriceJpaDto.fromModel(priceWithDate.getPrice());
        return dto;
    }

    @VisibleForTesting
    protected void setPrice(@Nonnull PriceJpaDto price) {
        this.price = price;
    }

    @VisibleForTesting
    protected void setDate(@Nonnull LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "PriceWithDateJpaDto{" +
               "price=" + price +
               ", date=" + date +
               '}';
    }
}
