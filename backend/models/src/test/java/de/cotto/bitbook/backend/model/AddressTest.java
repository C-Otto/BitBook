package de.cotto.bitbook.backend.model;

import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressFixtures.P2PKH;
import static de.cotto.bitbook.backend.model.AddressFixtures.P2SH;
import static de.cotto.bitbook.backend.model.AddressFixtures.P2TR;
import static de.cotto.bitbook.backend.model.AddressFixtures.P2WPKH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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

    @Test
    void comparable_smaller() {
        assertThat(new Address("a").compareTo(new Address("b"))).isNegative();
    }

    @Test
    void comparable_larger() {
        assertThat(new Address("b").compareTo(new Address("a"))).isPositive();
    }

    @Test
    void getScript_base58_p2pkh() {
        assertThat(P2PKH.getScript()).isEqualTo("76a91462e907b15cbf27d5425399ebf6f0fb50ebb88f1888ac");
    }

    @Test
    void getScript_base58_p2sh() {
        assertThat(P2SH.getScript()).isEqualTo("a91421ef2f4b1ea1f9ed09c1128d1ebb61d4729ca7d687");
    }

    @Test
    void getScript_bech32_p2wpkh() {
        assertThat(P2WPKH.getScript()).isEqualTo("0014aabf2ef8b9fe76fecf8701e11baa7a3f8792cba6");
    }

    @Test
    void getScript_bech32_p2tr() {
        assertThat(P2TR.getScript()).isEqualTo("5120a37c3903c8d0db6512e2b40b0dffa05e5a3ab73603ce8c9c4b7771e5412328f9");
    }

    @Test
    void getScript_unsupported() {
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> new Address("foo").getScript());
    }
}