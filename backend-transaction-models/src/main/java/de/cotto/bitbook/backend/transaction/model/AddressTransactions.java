package de.cotto.bitbook.backend.transaction.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class AddressTransactions {
    private static final int MAX_SHOWN_TRANSACTION_HASHES = 10;

    public static final AddressTransactions UNKNOWN = new AddressTransactions("", Collections.emptySet(), 0);
    private final String address;
    private final Set<String> transactionHashes;
    private final int lastCheckedAtBlockHeight;

    public AddressTransactions(
            String address,
            Set<String> transactionHashes,
            int lastCheckedAtBlockHeight
    ) {
        this.address = address;
        this.transactionHashes = new LinkedHashSet<>(transactionHashes);
        this.lastCheckedAtBlockHeight = lastCheckedAtBlockHeight;
    }

    public String getAddress() {
        return address;
    }

    public Set<String> getTransactionHashes() {
        return Collections.unmodifiableSet(transactionHashes);
    }

    public int getLastCheckedAtBlockHeight() {
        return lastCheckedAtBlockHeight;
    }

    public boolean isValid() {
        return !address.isEmpty();
    }

    public AddressTransactions getCombined(AddressTransactions update) {
        Set<String> combinedTransactionHashes = new HashSet<>();
        combinedTransactionHashes.addAll(transactionHashes);
        combinedTransactionHashes.addAll(update.transactionHashes);
        int newLastCheckedAtBlockHeight = Math.max(lastCheckedAtBlockHeight, update.lastCheckedAtBlockHeight);
        return new AddressTransactions(
                address,
                combinedTransactionHashes,
                newLastCheckedAtBlockHeight
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        AddressTransactions that = (AddressTransactions) other;

        if (lastCheckedAtBlockHeight != that.lastCheckedAtBlockHeight) {
            return false;
        }
        if (!address.equals(that.address)) {
            return false;
        }
        return transactionHashes.equals(that.transactionHashes);
    }

    @Override
    public int hashCode() {
        int result = address.hashCode();
        result = 31 * result + transactionHashes.hashCode();
        result = 31 * result + lastCheckedAtBlockHeight;
        return result;
    }

    @Override
    public String toString() {
        String hashes;
        if (transactionHashes.size() > MAX_SHOWN_TRANSACTION_HASHES) {
            hashes = "(" + transactionHashes.size() + " transactions)";
        } else {
            hashes = transactionHashes.toString();
        }
        return "AddressTransactions{" +
               "address='" + address + '\'' +
               ", transactionHashes='" + hashes + '\'' +
               ", lastCheckedAtBlockHeight='" + lastCheckedAtBlockHeight + '\'' +
               '}';
    }
}
