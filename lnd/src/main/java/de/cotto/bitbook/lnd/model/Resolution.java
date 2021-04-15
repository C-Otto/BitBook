package de.cotto.bitbook.lnd.model;

import java.util.Objects;

public class Resolution {
    private final String sweepTransactionHash;

    public Resolution(String sweepTransactionHash) {
        this.sweepTransactionHash = sweepTransactionHash;
    }

    public String getSweepTransactionHash() {
        return sweepTransactionHash;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Resolution that = (Resolution) other;
        return Objects.equals(sweepTransactionHash, that.sweepTransactionHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sweepTransactionHash);
    }

    @Override
    public String toString() {
        return "Resolution{" +
               "sweepTransactionHash='" + sweepTransactionHash + '\'' +
               '}';
    }
}
