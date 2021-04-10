package de.cotto.bitbook.cli;

import java.util.Objects;
import java.util.regex.Pattern;

public class CliTransactionHash {
    public static final String ERROR_MESSAGE = "Expected: 64 hex characters";

    private final String transactionHash;
    private static final Pattern PATTERN = Pattern.compile("[0-9a-fA-F]{64}");

    public CliTransactionHash(String transactionHash) {
        if (transactionHash.contains("\u00a0")) {
            this.transactionHash = sanitize(transactionHash.substring(0, transactionHash.indexOf('\u00a0')));
        } else {
            this.transactionHash = sanitize(transactionHash);
        }
    }

    private String sanitize(String transactionHash) {
        String sanitized = transactionHash.replaceAll("[^0-9a-fA-F]", "");
        if (PATTERN.matcher(sanitized).matches()) {
            return sanitized;
        }
        return "";
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        CliTransactionHash that = (CliTransactionHash) other;
        return Objects.equals(transactionHash, that.transactionHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionHash);
    }

    @Override
    public String toString() {
        return transactionHash;
    }
}
