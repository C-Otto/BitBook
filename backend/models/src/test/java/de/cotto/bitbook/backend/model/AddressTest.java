package de.cotto.bitbook.backend.model;

import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;

class AddressTest {
    @Test
    void none() {
        assertThat(Address.NONE).isEqualTo(new Address(""));
    }

    @Test
    void isValid_none() {
        assertThat(Address.NONE.isValid()).isFalse();
    }

    @Test
    void isValid() {
        assertThat(ADDRESS.isValid()).isTrue();
    }

    @Test
    void isInvalid_none() {
        assertThat(Address.NONE.isInvalid()).isTrue();
    }

    @Test
    void isInvalid() {
        assertThat(ADDRESS.isInvalid()).isFalse();
    }

    @Test
    void testToString() {
        assertThat(ADDRESS).hasToString("1DEP8i3QJCsomS4BSMY2RpU1upv62aGvhD");
    }
}