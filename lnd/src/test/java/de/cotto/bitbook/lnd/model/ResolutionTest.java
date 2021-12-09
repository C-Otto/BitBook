package de.cotto.bitbook.lnd.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResolutionTest {

    private static final String COMMIT = "COMMIT";
    private static final String ANCHOR = "ANCHOR";
    private static final String CLAIMED = "CLAIMED";
    private static final String FIRST_STAGE = "FIRST_STAGE";
    private static final String TIMEOUT = "TIMEOUT";
    private static final String INCOMING_HTLC = "INCOMING_HTLC";
    private static final String OUTGOING_HTLC = "OUTGOING_HTLC";
    private static final String SWEEP_TRANSACTION_HASH = "bar";
    private static final Resolution RESOLUTION_COMMIT_CLAIMED = new Resolution(SWEEP_TRANSACTION_HASH, COMMIT, CLAIMED);
    private static final Resolution RESOLUTION_ANCHOR_CLAIMED = new Resolution(SWEEP_TRANSACTION_HASH, ANCHOR, CLAIMED);

    @Test
    void sweepTransactionClaimsFunds_commit_claimed() {
        assertThat(RESOLUTION_COMMIT_CLAIMED.sweepTransactionClaimsFunds()).isTrue();
    }

    @Test
    void sweepTransactionClaimsFunds_anchor_claimed() {
        assertThat(RESOLUTION_ANCHOR_CLAIMED.sweepTransactionClaimsFunds()).isTrue();
    }

    @Test
    void sweepTransactionClaimsFunds_incoming_htlc_claimed() {
        assertThat(new Resolution(SWEEP_TRANSACTION_HASH, INCOMING_HTLC, CLAIMED)
                .sweepTransactionClaimsFunds()).isTrue();
    }

    @Test
    void sweepTransactionClaimsFunds_incoming_htlc_timeout() {
        assertThat(new Resolution(SWEEP_TRANSACTION_HASH, INCOMING_HTLC, TIMEOUT)
                .sweepTransactionClaimsFunds()).isFalse();
    }

    @Test
    void sweepTransactionClaimsFunds_outgoing_htlc_claimed() {
        assertThat(new Resolution(SWEEP_TRANSACTION_HASH, OUTGOING_HTLC, CLAIMED)
                .sweepTransactionClaimsFunds()).isFalse();
    }

    @Test
    void sweepTransactionClaimsFunds_outgoing_htlc_timeout() {
        assertThat(new Resolution(SWEEP_TRANSACTION_HASH, OUTGOING_HTLC, TIMEOUT)
                .sweepTransactionClaimsFunds()).isTrue();
    }

    @Test
    void sweepTransactionClaimsFunds_outgoing_htlc_first_stage() {
        assertThat(new Resolution(SWEEP_TRANSACTION_HASH, OUTGOING_HTLC, FIRST_STAGE)
                .sweepTransactionClaimsFunds()).isTrue();
    }

    @Test
    void getSweepTransaction() {
        assertThat(RESOLUTION_COMMIT_CLAIMED.getSweepTransactionHash()).isEqualTo(SWEEP_TRANSACTION_HASH);
    }

    @Test
    void getResolutionType() {
        assertThat(RESOLUTION_COMMIT_CLAIMED.getResolutionType()).isEqualTo(COMMIT);
    }

    @Test
    void getOutcome() {
        assertThat(RESOLUTION_COMMIT_CLAIMED.getOutcome()).isEqualTo(CLAIMED);
    }

    @Test
    void testToString() {
        assertThat(RESOLUTION_COMMIT_CLAIMED)
                .hasToString("Resolution{" +
                             "sweepTransactionHash='bar'" +
                             ", resolutionType='COMMIT'" +
                             ", outcome='CLAIMED'" +
                             "}");
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Resolution.class).usingGetClass().verify();
    }
}