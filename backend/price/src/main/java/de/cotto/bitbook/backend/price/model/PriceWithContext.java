package de.cotto.bitbook.backend.price.model;

import javax.annotation.Nonnull;

public class PriceWithContext {
    private final Price price;
    private final PriceContext priceContext;

    public PriceWithContext(@Nonnull Price price, @Nonnull PriceContext priceContext) {
        this.price = price;
        this.priceContext = priceContext;
    }

    public Price getPrice() {
        return price;
    }

    public PriceContext getPriceContext() {
        return priceContext;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        PriceWithContext that = (PriceWithContext) other;

        if (!price.equals(that.price)) {
            return false;
        }
        return priceContext.equals(that.priceContext);
    }

    @Override
    public int hashCode() {
        int result = price.hashCode();
        result = 31 * result + priceContext.hashCode();
        return result;
    }
}
