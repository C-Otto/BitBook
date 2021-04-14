package de.cotto.bitbook.lnd.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InitiatorTest {
    @Test
    void testToString_remote() {
        assertThat(Initiator.REMOTE).hasToString("remote");
    }

    @Test
    void testToString_local() {
        assertThat(Initiator.LOCAL).hasToString("local");
    }

    @Test
    void testToString_unknown() {
        assertThat(Initiator.UNKNOWN).hasToString("unknown");
    }

    @Test
    void fromString_local() {
        assertThat(Initiator.fromString("INITIATOR_LOCAL")).isEqualTo(Initiator.LOCAL);
    }

    @Test
    void fromString_remote() {
        assertThat(Initiator.fromString("INITIATOR_REMOTE")).isEqualTo(Initiator.REMOTE);
    }

    @Test
    void fromString_unknown() {
        assertThat(Initiator.fromString("INITIATOR_UNKNOWN")).isEqualTo(Initiator.UNKNOWN);
    }
}