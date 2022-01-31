package de.cotto.bitbook.backend.price.persistence;

import com.google.common.annotations.VisibleForTesting;
import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.price.model.PriceContext;
import de.cotto.bitbook.backend.price.model.PriceWithContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@IdClass(PriceWithContextId.class)
@Table(name = "prices")
class PriceWithContextJpaDto {
    @Nullable
    @Embedded
    private PriceJpaDto price;

    @Id
    @Nullable
    private LocalDate date;

    @Id
    @Nullable
    private String chain;

    PriceWithContextJpaDto() {
        // for JPA
    }

    protected PriceWithContext toModel() {
        LocalDate localDate = Objects.requireNonNull(date);
        PriceContext priceContext = new PriceContext(localDate, Chain.valueOf(Objects.requireNonNull(chain)));
        return new PriceWithContext(Objects.requireNonNull(price).toModel(), priceContext);
    }

    protected static PriceWithContextJpaDto fromModel(PriceWithContext priceWithContext) {
        PriceWithContextJpaDto dto = new PriceWithContextJpaDto();
        dto.date = priceWithContext.getPriceContext().date();
        dto.chain = priceWithContext.getPriceContext().chain().toString();
        dto.price = PriceJpaDto.fromModel(priceWithContext.getPrice());
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

    @VisibleForTesting
    protected void setChain(@Nonnull String chain) {
        this.chain = chain;
    }

    @Override
    public String toString() {
        return "PriceWithDateJpaDto{" +
                "price=" + price +
                ", date=" + date +
                ", chain=" + chain +
                '}';
    }
}
