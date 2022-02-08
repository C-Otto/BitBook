package de.cotto.bitbook.lnd;

import de.cotto.bitbook.backend.model.TransactionHash;

public final class ChannelPointParser {
    private ChannelPointParser() {
        // utility class
    }

    public static TransactionHash getTransactionHash(String channelPoint) {
        return new TransactionHash(channelPoint.substring(0, channelPoint.indexOf(':')));
    }

    public static int getOutputIndex(String channelPoint) {
        return Integer.parseInt(channelPoint.substring(channelPoint.indexOf(':') + 1));
    }
}
