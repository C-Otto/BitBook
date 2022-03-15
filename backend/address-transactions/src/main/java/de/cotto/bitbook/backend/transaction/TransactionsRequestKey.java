package de.cotto.bitbook.backend.transaction;

import com.google.common.base.Preconditions;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.Chain;

public record TransactionsRequestKey(
        Address address,
        Chain chain,
        int blockHeight,
        AddressTransactions addressTransactions
) {
    public TransactionsRequestKey(Address address, Chain chain, int blockHeight) {
        this(address, chain, blockHeight, AddressTransactions.unknown(chain));
    }

    public TransactionsRequestKey(AddressTransactions addressTransactions, int blockHeight) {
        this(addressTransactions.getAddress(), addressTransactions.getChain(), blockHeight, addressTransactions);
    }

    public boolean hasKnownAddressTransactions() {
        return !AddressTransactions.unknown(addressTransactions.getChain()).equals(addressTransactions);
    }

    @Override
    public AddressTransactions addressTransactions() {
        Preconditions.checkArgument(hasKnownAddressTransactions());
        return addressTransactions;
    }
}
