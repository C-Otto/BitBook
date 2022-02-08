package de.cotto.bitbook.backend.transaction.deserialization;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.TransactionHash;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class AddressTransactionsDto {
    private final Address address;
    private final Set<TransactionHash> transactionHashes;

    protected AddressTransactionsDto(Address address, Set<TransactionHash> transactionHashes) {
        this.address = address;
        this.transactionHashes = transactionHashes;
    }

    public AddressTransactions toModel(int lastCheckedAtBlockheight, Address expectedAddress) {
        validateAddress(expectedAddress);
        return new AddressTransactions(
                requireNonNull(expectedAddress),
                requireNonNull(transactionHashes),
                lastCheckedAtBlockheight
        );
    }

    public Address getAddress() {
        return address;
    }

    public Set<TransactionHash> getTransactionHashes() {
        return transactionHashes;
    }

    protected void validateAddress(Address expectedAddress) {
        if (!expectedAddress.equals(address)) {
            throw new IllegalStateException();
        }
    }
}
