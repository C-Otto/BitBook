package de.cotto.bitbook.backend.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public record AddressTransactions(
        Address address,
        Set<TransactionHash> transactionHashes,
        int lastCheckedAtBlockHeight,
        Chain chain
) {
    private static final int MAX_SHOWN_TRANSACTION_HASHES = 10;

    public AddressTransactions {
        transactionHashes = new LinkedHashSet<>(transactionHashes);
    }

    public static AddressTransactions unknown(Chain chain) {
        return new AddressTransactions(Address.NONE, Set.of(), 0, chain);
    }

    @Override
    public Set<TransactionHash> transactionHashes() {
        return Collections.unmodifiableSet(transactionHashes);
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
