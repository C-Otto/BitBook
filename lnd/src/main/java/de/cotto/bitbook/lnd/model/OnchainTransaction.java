package de.cotto.bitbook.lnd.model;

import de.cotto.bitbook.backend.transaction.model.Coins;

import java.util.Objects;

public class OnchainTransaction {
    private final String transactionHash;
    private final String label;
    private final Coins amount;
    private final Coins fees;

    public OnchainTransaction(String transactionHash, String label, Coins amount, Coins fees) {
        this.transactionHash = transactionHash;
        this.label = label;
        this.amount = amount;
        this.fees = fees;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public String getLabel() {
        return label;
    }

    public Coins getAmount() {
        return amount;
    }

    public Coins getFees() {
        return fees;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        OnchainTransaction that = (OnchainTransaction) other;
        return Objects.equals(transactionHash, that.transactionHash)
               && Objects.equals(label, that.label)
               && Objects.equals(amount, that.amount)
               && Objects.equals(fees, that.fees);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionHash, label, amount, fees);
    }

    @Override
    public String toString() {
        return "OnchainTransaction{" +
               "transactionHash='" + transactionHash + '\'' +
               ", label='" + label + '\'' +
               ", amount=" + amount +
               ", fees=" + fees +
               '}';
    }
}
