package de.cotto.bitbook.lnd;

public final class ChannelPointParser {
    private ChannelPointParser() {
        // utility class
    }

    public static String getTransactionHash(String channelPoint) {
        return channelPoint.substring(0, channelPoint.indexOf(':'));
    }

    public static int getOutputIndex(String channelPoint) {
        return Integer.parseInt(channelPoint.substring(channelPoint.indexOf(':') + 1));
    }
}
