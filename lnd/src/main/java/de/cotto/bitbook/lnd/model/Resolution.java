package de.cotto.bitbook.lnd.model;

public record Resolution(
        String sweepTransactionHash,
        String resolutionType,
        String outcome
) {
    private static final String OUTGOING_HTLC = "OUTGOING_HTLC";
    private static final String INCOMING_HTLC = "INCOMING_HTLC";
    private static final String TIMEOUT = "TIMEOUT";
    private static final String CLAIMED = "CLAIMED";

    public boolean sweepTransactionClaimsFunds() {
        if (OUTGOING_HTLC.equals(resolutionType) && CLAIMED.equals(outcome)) {
            return false;
        }
        return !(INCOMING_HTLC.equals(resolutionType) && TIMEOUT.equals(outcome));
    }
}
