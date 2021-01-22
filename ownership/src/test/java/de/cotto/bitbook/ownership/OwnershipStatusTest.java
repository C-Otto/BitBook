package de.cotto.bitbook.ownership;

import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.ownership.OwnershipStatus.FOREIGN;
import static de.cotto.bitbook.ownership.OwnershipStatus.OWNED;
import static de.cotto.bitbook.ownership.OwnershipStatus.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;

class OwnershipStatusTest {
    @Test
    void unknown() {
        assertThat(OwnershipStatus.valueOf("UNKNOWN")).isEqualTo(UNKNOWN);
    }

    @Test
    void owned() {
        assertThat(OwnershipStatus.valueOf("OWNED")).isEqualTo(OWNED);
    }

    @Test
    void foreign() {
        assertThat(OwnershipStatus.valueOf("FOREIGN")).isEqualTo(FOREIGN);
    }
}