package de.cotto.bitbook.backend.price.persistence;

import de.cotto.bitbook.backend.price.model.PriceContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class PriceWithContextId implements Serializable {
    @Nullable
    private LocalDate date;

    @Nullable
    private String chain;

    public PriceWithContextId() {
        // for JPA
    }

    public PriceWithContextId(@Nonnull LocalDate date, @Nonnull String chain) {
        this.date = date;
        this.chain = chain;
    }

    public static PriceWithContextId fromModel(PriceContext priceContext) {
        return new PriceWithContextId(priceContext.date(), priceContext.chain().toString());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        PriceWithContextId that = (PriceWithContextId) other;
        return Objects.equals(date, that.date) && Objects.equals(chain, that.chain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, chain);
    }
}
