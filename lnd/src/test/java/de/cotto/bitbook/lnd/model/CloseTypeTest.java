package de.cotto.bitbook.lnd.model;

import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.lnd.model.CloseType.COOPERATIVE;
import static de.cotto.bitbook.lnd.model.CloseType.COOPERATIVE_LOCAL;
import static de.cotto.bitbook.lnd.model.CloseType.COOPERATIVE_REMOTE;
import static de.cotto.bitbook.lnd.model.CloseType.FORCE_LOCAL;
import static de.cotto.bitbook.lnd.model.CloseType.FORCE_REMOTE;
import static org.assertj.core.api.Assertions.assertThat;

class CloseTypeTest {
    @Test
    void testToString_cooperative() {
        assertThat(COOPERATIVE).hasToString("cooperative");
    }

    @Test
    void testToString_cooperative_remote() {
        assertThat(COOPERATIVE_REMOTE).hasToString("cooperative remote");
    }

    @Test
    void testToString_cooperative_local() {
        assertThat(COOPERATIVE_LOCAL).hasToString("cooperative local");
    }

    @Test
    void testToString_force_remote() {
        assertThat(FORCE_REMOTE).hasToString("force remote");
    }

    @Test
    void testToString_force_local() {
        assertThat(FORCE_LOCAL).hasToString("force local");
    }

    @Test
    void fromStringAndInitiator_cooperative_unknown() {
        assertThat(CloseType.fromStringAndInitiator("COOPERATIVE_CLOSE", "INITIATOR_UNKNOWN"))
                .isEqualTo(COOPERATIVE);
    }

    @Test
    void fromStringAndInitiator_cooperative_local() {
        assertThat(CloseType.fromStringAndInitiator("COOPERATIVE_CLOSE", "INITIATOR_LOCAL"))
                .isEqualTo(COOPERATIVE_LOCAL);
    }

    @Test
    void fromStringAndInitiator_cooperative_remote() {
        assertThat(CloseType.fromStringAndInitiator("COOPERATIVE_CLOSE", "INITIATOR_REMOTE"))
                .isEqualTo(COOPERATIVE_REMOTE);
    }

    @Test
    void fromStringAndInitiator_force_remote() {
        assertThat(CloseType.fromStringAndInitiator("REMOTE_FORCE_CLOSE", "INITIATOR_REMOTE"))
                .isEqualTo(FORCE_REMOTE);
    }

    @Test
    void fromStringAndInitiator_force_local() {
        assertThat(CloseType.fromStringAndInitiator("LOCAL_FORCE_CLOSE", "INITIATOR_LOCAL"))
                .isEqualTo(FORCE_LOCAL);
    }
}