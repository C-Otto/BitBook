package de.cotto.bitbook.backend.transaction;

import com.google.common.base.Preconditions;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.Chain;

import java.util.Objects;

public class TransactionsRequestKey {
    private final Address address;
    private final Chain chain;
    private final int blockHeight;
    private final AddressTransactions addressTransactions;

    public TransactionsRequestKey(Address address, Chain chain, int blockHeight) {
        this.address = address;
        this.chain = chain;
        this.blockHeight = blockHeight;
        this.addressTransactions = AddressTransactions.unknown(chain);
    }

    public TransactionsRequestKey(AddressTransactions addressTransactions, int blockHeight) {
        this.address = addressTransactions.getAddress();
        this.chain = addressTransactions.getChain();
        this.blockHeight = blockHeight;
        this.addressTransactions = addressTransactions;
    }

    public Address getAddress() {
        return address;
    }

    public Chain getChain() {
        return chain;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public boolean hasKnownAddressTransactions() {
        return !AddressTransactions.unknown(addressTransactions.getChain()).equals(addressTransactions);
    }

    public AddressTransactions getAddressTransactions() {
        Preconditions.checkArgument(hasKnownAddressTransactions());
        return addressTransactions;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        TransactionsRequestKey that = (TransactionsRequestKey) other;
        return blockHeight == that.blockHeight
               && Objects.equals(chain, that.chain)
               && Objects.equals(address, that.address)
               && Objects.equals(addressTransactions, that.addressTransactions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, chain, blockHeight, addressTransactions);
    }

    @Override
    public String toString() {
        return "TransactionsRequestKey{" +
               "address='" + address + '\'' +
               ", addressTransactions='" + addressTransactions + '\'' +
               ", blockHeight=" + blockHeight +
               ", chain='" + chain + '\'' +
               '}';
    }
}
