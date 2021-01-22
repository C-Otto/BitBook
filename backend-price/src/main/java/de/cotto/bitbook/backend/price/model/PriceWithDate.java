package de.cotto.bitbook.backend.price.model;

import java.time.LocalDate;

public class PriceWithDate {
    private final Price price;

    private final LocalDate date;

    public PriceWithDate(Price price, LocalDate date) {
        this.price = price;
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public Price getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        PriceWithDate that = (PriceWithDate) other;

        if (!price.equals(that.price)) {
            return false;
        }
        return date.equals(that.date);
    }

    @Override
    public int hashCode() {
        int result = price.hashCode();
        result = 31 * result + date.hashCode();
        return result;
    }
}
