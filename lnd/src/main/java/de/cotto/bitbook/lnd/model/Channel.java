package de.cotto.bitbook.lnd.model;

import de.cotto.bitbook.backend.transaction.model.Transaction;

import java.util.Objects;

public class Channel {
    private final boolean initiator;
    private final String remotePubkey;
    private final Transaction openingTransaction;
    private final int outputIndex;

    public Channel(boolean initiator, String remotePubkey, Transaction openingTransaction, int outputIndex) {
        this.initiator = initiator;
        this.remotePubkey = remotePubkey;
        this.openingTransaction = openingTransaction;
        this.outputIndex = outputIndex;
    }

    public boolean isInitiator() {
        return initiator;
    }

    public String getRemotePubkey() {
        return remotePubkey;
    }

    public Transaction getOpeningTransaction() {
        return openingTransaction;
    }

    public String getChannelAddress() {
        return openingTransaction.getOutputs().get(outputIndex).getAddress();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Channel channel = (Channel) other;
        return initiator == channel.initiator
               && outputIndex == channel.outputIndex
               && Objects.equals(remotePubkey, channel.remotePubkey)
               && Objects.equals(openingTransaction, channel.openingTransaction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(initiator, remotePubkey, openingTransaction, outputIndex);
    }
}
