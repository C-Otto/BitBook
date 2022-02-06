package de.cotto.bitbook.lnd.model;

import de.cotto.bitbook.backend.model.Coins;

import java.util.Objects;

public class PoolLease {
    private final String transactionHash;
    private final int outputIndex;
    private final String pubkey;
    private final Coins premium;
    private final Coins executionFee;
    private final Coins chainFee;

    public PoolLease(
            String transactionHash,
            int outputIndex,
            String pubkey,
            Coins premium,
            Coins executionFee,
            Coins chainFee
    ) {
        this.transactionHash = transactionHash;
        this.outputIndex = outputIndex;
        this.pubkey = pubkey;
        this.premium = premium;
        this.executionFee = executionFee;
        this.chainFee = chainFee;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public int getOutputIndex() {
        return outputIndex;
    }

    public String getPubKey() {
        return pubkey;
    }

    public Coins getPremiumWithoutFees() {
        return premium.subtract(chainFee).subtract(executionFee);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        PoolLease poolLease = (PoolLease) other;
        return outputIndex == poolLease.outputIndex
               && Objects.equals(transactionHash, poolLease.transactionHash)
               && Objects.equals(pubkey, poolLease.pubkey)
               && Objects.equals(premium, poolLease.premium)
               && Objects.equals(executionFee, poolLease.executionFee)
               && Objects.equals(chainFee, poolLease.chainFee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionHash, outputIndex, pubkey, premium, executionFee, chainFee);
    }
}
