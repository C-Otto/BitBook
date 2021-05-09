package de.cotto.bitbook.backend.price.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Price {
    public static final Price UNKNOWN = Price.of(-1);

    private static final int SCALE = 8;

    private final BigDecimal asBigDecimal;

    private Price(BigDecimal bigDecimal) {
        asBigDecimal = bigDecimal.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static Price of(BigDecimal price) {
        return new Price(price);
    }

    public static Price of(long price) {
        return new Price(BigDecimal.valueOf(price));
    }

    public static Price of(double price) {
        return new Price(BigDecimal.valueOf(price));
    }

    public Price add(Price addend) {
        if (this.equals(UNKNOWN) || addend.equals(UNKNOWN)) {
            throw new UnsupportedOperationException();
        }
        return Price.of(Objects.requireNonNull(asBigDecimal).add(Objects.requireNonNull(addend.asBigDecimal)));
    }

    public Price subtract(Price subtrahend) {
        if (this.equals(UNKNOWN) || subtrahend.equals(UNKNOWN)) {
            throw new UnsupportedOperationException();
        }
        return Price.of(Objects.requireNonNull(asBigDecimal).subtract(Objects.requireNonNull(subtrahend.asBigDecimal)));
    }

    public BigDecimal getAsBigDecimal() {
        if (this.equals(UNKNOWN)) {
            throw new UnsupportedOperationException();
        }
        return Objects.requireNonNull(asBigDecimal);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Price price = (Price) other;
        return Objects.equals(asBigDecimal, price.asBigDecimal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(asBigDecimal);
    }

    @Override
    public String toString() {
        if (this.equals(UNKNOWN)) {
            return "Price{UNKNOWN}";
        }
        return "Price{" + asBigDecimal + '}';
    }
}
