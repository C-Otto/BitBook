package de.cotto.bitbook.backend.model;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Objects;

public class TransactionWithDescription implements Comparable<TransactionWithDescription> {
    private static final int MAX_DESCRIPTION_LENGTH = 20;
    private final String transactionHash;
    private final String description;

    public TransactionWithDescription(String transactionHash) {
        this(transactionHash, "");
    }

    public TransactionWithDescription(String transactionHash, String description) {
        this.transactionHash = transactionHash;
        this.description = description;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        TransactionWithDescription that = (TransactionWithDescription) other;
        return Objects.equals(transactionHash, that.transactionHash)
               && Objects.equals(description, that.description);
    }

    @Override
    public int compareTo(@Nonnull TransactionWithDescription other) {
        return Comparator.comparing(TransactionWithDescription::getDescription)
                .thenComparing(TransactionWithDescription::getTransactionHash)
                .compare(this, other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionHash, description);
    }

    @Override
    public String toString() {
        return transactionHash + ' ' + getFormattedDescription();
    }

    public String getFormattedDescription() {
        return padOrShorten(description);
    }

    private String padOrShorten(String string) {
        if (string.length() > MAX_DESCRIPTION_LENGTH) {
            return string.substring(0, MAX_DESCRIPTION_LENGTH - 1) + "…";
        } else {
            return StringUtils.leftPad(string, MAX_DESCRIPTION_LENGTH);
        }
    }
}
