package de.cotto.bitbook.lnd.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResolutionTest {
    @Test
    void getSweepTransaction() {
        assertThat(new Resolution("bar").getSweepTransactionHash()).isEqualTo("bar");
    }

    @Test
    void testToString() {
        assertThat(new Resolution("foo"))
                .hasToString("Resolution{" +
                             "sweepTransactionHash='foo'" +
                             "}");
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Resolution.class).usingGetClass().verify();
    }
}