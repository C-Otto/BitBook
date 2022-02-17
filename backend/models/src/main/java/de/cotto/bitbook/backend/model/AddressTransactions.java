package de.cotto.bitbook.backend.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class AddressTransactions {
    private static final int MAX_SHOWN_TRANSACTION_HASHES = 10;

    private final Address address;
    private final Set<TransactionHash> transactionHashes;
    private final int lastCheckedAtBlockHeight;
    private final Chain chain;

    public AddressTransactions(
            Address address,
            Set<TransactionHash> transactionHashes,
            int lastCheckedAtBlockHeight,
            Chain chain
    ) {
        this.address = address;
        this.transactionHashes = new LinkedHashSet<>(transactionHashes);
        this.lastCheckedAtBlockHeight = lastCheckedAtBlockHeight;
        this.chain = chain;
    }

    public static AddressTransactions unknown(Chain chain) {
        return new AddressTransactions(Address.NONE, Set.of(), 0, chain);
    }

    public Address getAddress() {
        return address;
    }

    public Set<TransactionHash> getTransactionHashes() {
        return Collections.unmodifiableSet(transactionHashes);
    }

    public int getLastCheckedAtBlockHeight() {
        return lastCheckedAtBlockHeight;
    }

    public Chain getChain() {
        return chain;
    }

    public boolean isValid() {
        return address.isValid();
    }

    public AddressTransactions getCombined(AddressTransactions update) {
        throwIfDifferentChain(update);
        Set<TransactionHash> combinedTransactionHashes = new HashSet<>();
        combinedTransactionHashes.addAll(transactionHashes);
        combinedTransactionHashes.addAll(update.transactionHashes);
        int newLastCheckedAtBlockHeight = Math.max(lastCheckedAtBlockHeight, update.lastCheckedAtBlockHeight);
        return new AddressTransactions(
                address,
                combinedTransactionHashes,
                newLastCheckedAtBlockHeight,
                chain
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
        if (!chain.equals(that.chain)) {
            return false;
        }
        return transactionHashes.equals(that.transactionHashes);
    }

    @Override
    public int hashCode() {
        int result = address.hashCode();
        result = 31 * result + transactionHashes.hashCode();
        result = 31 * result + lastCheckedAtBlockHeight;
        result = 31 * result + chain.hashCode();
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
               ", chain='" + chain + '\'' +
               '}';
    }

    private void throwIfDifferentChain(AddressTransactions update) {
        if (!update.chain.equals(chain)) {
            throw new IllegalArgumentException();
        }
    }
}
