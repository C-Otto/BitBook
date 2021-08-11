package de.cotto.bitbook.lnd.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResolutionTest {

    private static final Resolution RESOLUTION = new Resolution("bar", "resolutionType", "outcome");

    @Test
    void getSweepTransaction() {
        assertThat(RESOLUTION.getSweepTransactionHash()).isEqualTo("bar");
    }

    @Test
    void getResolutionType() {
        assertThat(RESOLUTION.getResolutionType()).isEqualTo("resolutionType");
    }

    @Test
    void getOutcome() {
        assertThat(RESOLUTION.getOutcome()).isEqualTo("outcome");
    }

    @Test
    void testToString() {
        assertThat(RESOLUTION).hasToString("Resolution{" +
                                           "sweepTransactionHash='bar'" +
                                           ", resolutionType='resolutionType'" +
                                           ", outcome='outcome'" +
                                           "}");
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Resolution.class).usingGetClass().verify();
    }
}