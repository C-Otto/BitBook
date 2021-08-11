package de.cotto.bitbook.lnd.model;

import java.util.Objects;

public class Resolution {
    private final String sweepTransactionHash;
    private final String resolutionType;
    private final String outcome;

    public Resolution(String sweepTransactionHash, String resolutionType, String outcome) {
        this.sweepTransactionHash = sweepTransactionHash;
        this.resolutionType = resolutionType;
        this.outcome = outcome;
    }

    public String getSweepTransactionHash() {
        return sweepTransactionHash;
    }

    public String getResolutionType() {
        return resolutionType;
    }

    public String getOutcome() {
        return outcome;
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
        return Objects.equals(sweepTransactionHash, that.sweepTransactionHash)
               && Objects.equals(resolutionType, that.resolutionType)
               && Objects.equals(outcome, that.outcome
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(sweepTransactionHash, resolutionType, outcome);
    }

    @Override
    public String toString() {
        return "Resolution{" +
               "sweepTransactionHash='" + sweepTransactionHash + '\'' +
               ", resolutionType='" + resolutionType + '\'' +
               ", outcome='" + outcome + '\'' +
               '}';
    }
}
