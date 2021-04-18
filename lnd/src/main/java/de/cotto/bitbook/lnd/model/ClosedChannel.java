package de.cotto.bitbook.lnd.model;

import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Output;
import de.cotto.bitbook.backend.transaction.model.Transaction;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ClosedChannel {
    private static final String BITCOIN_GENESIS_BLOCK_HASH
            = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f";

    private final String chainHash;
    private final Transaction openingTransaction;
    private final Transaction closingTransaction;
    private final String remotePubkey;
    private final Coins settledBalance;
    private final Initiator openInitiator;
    private final CloseType closeType;
    private final Set<Resolution> resolutions;

    private ClosedChannel(
            String chainHash,
            Transaction openingTransaction,
            Transaction closingTransaction,
            String remotePubkey,
            Coins settledBalance,
            Initiator openInitiator,
            CloseType closeType,
            Set<Resolution> resolutions
    ) {
        this.chainHash = chainHash;
        this.openingTransaction = openingTransaction;
        this.closingTransaction = closingTransaction;
        this.remotePubkey = remotePubkey;
        this.settledBalance = settledBalance;
        this.openInitiator = openInitiator;
        this.closeType = closeType;
        this.resolutions = resolutions;
    }

    public Transaction getOpeningTransaction() {
        return openingTransaction;
    }

    public Transaction getClosingTransaction() {
        return closingTransaction;
    }

    public String getRemotePubkey() {
        return remotePubkey;
    }

    public Coins getSettledBalance() {
        return settledBalance;
    }

    public Initiator getOpenInitiator() {
        return openInitiator;
    }

    public String getChannelAddress() {
        return closingTransaction.getInputs().get(0).getAddress();
    }

    public CloseType getCloseType() {
        return closeType;
    }

    public Set<Resolution> getResolutions() {
        return resolutions;
    }

    public boolean isValid() {
        return BITCOIN_GENESIS_BLOCK_HASH.equals(chainHash)
               && !openingTransaction.isInvalid()
               && !closingTransaction.isInvalid()
               && closingTransaction.getInputs().size() == 1;
    }

    public ClosedChannelBuilder toBuilder() {
        return builder()
                .withChainHash(chainHash)
                .withOpeningTransaction(openingTransaction)
                .withClosingTransaction(closingTransaction)
                .withRemotePubkey(remotePubkey)
                .withSettledBalance(settledBalance)
                .withOpenInitiator(openInitiator)
                .withCloseType(closeType)
                .withResolutions(resolutions);
    }

    public static ClosedChannelBuilder builder() {
        return new ClosedChannelBuilder();
    }

    public Optional<String> getSettlementAddress() {
        return closingTransaction.getOutputWithValue(settledBalance).map(Output::getAddress);
    }

    @Override
    public String toString() {
        return "ClosedChannel{" +
               "chainHash='" + chainHash + '\'' +
               ", openingTransaction=" + openingTransaction +
               ", closingTransaction=" + closingTransaction +
               ", remotePubkey='" + remotePubkey + '\'' +
               ", settledBalance=" + settledBalance +
               ", openInitiator=" + openInitiator +
               ", closeType=" + closeType +
               ", resolutions=" + resolutions +
               '}';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ClosedChannel that = (ClosedChannel) other;
        return Objects.equals(chainHash, that.chainHash)
               && Objects.equals(openingTransaction, that.openingTransaction)
               && Objects.equals(closingTransaction, that.closingTransaction)
               && Objects.equals(remotePubkey, that.remotePubkey)
               && Objects.equals(settledBalance, that.settledBalance)
               && openInitiator == that.openInitiator
               && closeType == that.closeType
               && Objects.equals(resolutions, that.resolutions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                chainHash,
                openingTransaction,
                closingTransaction,
                remotePubkey,
                settledBalance,
                openInitiator,
                closeType,
                resolutions
        );
    }

    public static class ClosedChannelBuilder {
        private String chainHash = "";
        private Transaction openingTransaction = Transaction.UNKNOWN;
        private Transaction closingTransaction = Transaction.UNKNOWN;
        private String remotePubkey = "";
        private Coins settledBalance = Coins.NONE;
        private Initiator openInitiator = Initiator.UNKNOWN;
        private Set<Resolution> resolutions = new LinkedHashSet<>();

        @Nullable
        private CloseType closeType;

        private ClosedChannelBuilder() {
        }

        public ClosedChannelBuilder withChainHash(String chainHash) {
            this.chainHash = chainHash;
            return this;
        }

        public ClosedChannelBuilder withOpeningTransaction(Transaction openingTransaction) {
            this.openingTransaction = openingTransaction;
            return this;
        }

        public ClosedChannelBuilder withClosingTransaction(Transaction closingTransaction) {
            this.closingTransaction = closingTransaction;
            return this;
        }

        public ClosedChannelBuilder withRemotePubkey(String remotePubkey) {
            this.remotePubkey = remotePubkey;
            return this;
        }

        public ClosedChannelBuilder withSettledBalance(Coins settledBalance) {
            this.settledBalance = settledBalance;
            return this;
        }

        public ClosedChannelBuilder withOpenInitiator(Initiator openInitiator) {
            this.openInitiator = openInitiator;
            return this;
        }

        public ClosedChannelBuilder withCloseType(CloseType closeType) {
            this.closeType = closeType;
            return this;
        }

        public ClosedChannelBuilder withResolution(Resolution resolution) {
            this.resolutions.add(resolution);
            return this;
        }

        public ClosedChannelBuilder withResolutions(Set<Resolution> resolutions) {
            this.resolutions = resolutions;
            return this;
        }

        public ClosedChannel build() {
            return new ClosedChannel(
                    chainHash,
                    openingTransaction,
                    closingTransaction,
                    remotePubkey,
                    settledBalance,
                    openInitiator,
                    Objects.requireNonNull(closeType),
                    resolutions
            );
        }
    }
}
