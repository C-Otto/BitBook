package de.cotto.bitbook.backend.transaction.deserialization;

import de.cotto.bitbook.backend.model.AddressTransactions;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class AddressTransactionsDto {
    private final String address;
    private final Set<String> transactionHashes;

    protected AddressTransactionsDto(String address, Set<String> transactionHashes) {
        this.address = address;
        this.transactionHashes = transactionHashes;
    }

    public AddressTransactions toModel(int lastCheckedAtBlockheight, String expectedAddress) {
        validateAddress(expectedAddress);
        return new AddressTransactions(
                requireNonNull(expectedAddress),
                requireNonNull(transactionHashes),
                lastCheckedAtBlockheight
        );
    }

    public String getAddress() {
        return address;
    }

    public Set<String> getTransactionHashes() {
        return transactionHashes;
    }

    protected void validateAddress(String expectedAddress) {
        if (!expectedAddress.equals(address)) {
            throw new IllegalStateException();
        }
    }
}
