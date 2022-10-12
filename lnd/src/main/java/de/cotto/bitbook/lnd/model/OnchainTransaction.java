package de.cotto.bitbook.lnd.model;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.TransactionHash;

import java.util.Set;

public record OnchainTransaction(
        TransactionHash transactionHash,
        String label,
        Coins amount,
        Coins fees,
        Set<Address> ownedAddresses
) {

    public OnchainTransaction(TransactionHash transactionHash, String label, Coins amount, Coins fees) {
        this(transactionHash, label, amount, fees, Set.of());
    }

    public boolean hasLabel() {
        return !label.isBlank();
    }

    public boolean hasFees() {
        return fees.isPositive();
    }

    public Coins getAbsoluteAmountWithoutFees() {
        return amount.add(fees).absolute();
    }
}
