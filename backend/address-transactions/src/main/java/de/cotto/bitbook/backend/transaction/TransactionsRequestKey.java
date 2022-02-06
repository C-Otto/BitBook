package de.cotto.bitbook.backend.transaction;

import com.google.common.base.Preconditions;
import de.cotto.bitbook.backend.model.AddressTransactions;

import java.util.Objects;

public class TransactionsRequestKey {
    private final String address;
    private final int blockHeight;
    private final AddressTransactions addressTransactions;

    public TransactionsRequestKey(String address, int blockHeight) {
        this.address = address;
        this.blockHeight = blockHeight;
        this.addressTransactions = AddressTransactions.UNKNOWN;
    }

    public TransactionsRequestKey(AddressTransactions addressTransactions, int blockHeight) {
        this.address = addressTransactions.getAddress();
        this.blockHeight = blockHeight;
        this.addressTransactions = addressTransactions;
    }

    public String getAddress() {
        return address;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public boolean hasKnownAddressTransactions() {
        return !AddressTransactions.UNKNOWN.equals(addressTransactions);
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
               && Objects.equals(address, that.address)
               && Objects.equals(addressTransactions, that.addressTransactions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, blockHeight, addressTransactions);
    }

    @Override
    public String toString() {
        return "TransactionsRequestKey{" +
               "address='" + address + '\'' +
               ", addressTransactions='" + addressTransactions + '\'' +
               ", blockHeight=" + blockHeight +
               '}';
    }
}
