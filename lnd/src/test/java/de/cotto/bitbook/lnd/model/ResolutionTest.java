package de.cotto.bitbook.lnd.model;

import de.cotto.bitbook.backend.model.TransactionHash;
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
    private static final TransactionHash SWEEP_TRANSACTION_HASH = new TransactionHash("bar");
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
    void sweepTransaction() {
        assertThat(RESOLUTION_COMMIT_CLAIMED.sweepTransactionHash()).isEqualTo(SWEEP_TRANSACTION_HASH);
    }

    @Test
    void resolutionType() {
        assertThat(RESOLUTION_COMMIT_CLAIMED.resolutionType()).isEqualTo(COMMIT);
    }

    @Test
    void outcome() {
        assertThat(RESOLUTION_COMMIT_CLAIMED.outcome()).isEqualTo(CLAIMED);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Resolution.class).usingGetClass().verify();
    }
}