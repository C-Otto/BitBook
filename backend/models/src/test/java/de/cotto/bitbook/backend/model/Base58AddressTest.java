package de.cotto.bitbook.backend.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class Base58AddressTest {

    private static final String P2SH = AddressFixtures.P2SH.toString();
    private static final String P2SH_2 = AddressFixtures.P2SH_2.toString();
    private static final String P2PKH = AddressFixtures.P2PKH.toString();
    private static final String P2WPKH = AddressFixtures.P2WPKH.toString();

    @Test
    void getScript_p2pkh() {
        assertThat(new Base58Address(P2PKH).getScript())
                .isEqualTo(new HexString("76a91462e907b15cbf27d5425399ebf6f0fb50ebb88f1888ac"));
    }

    @Test
    void getScript_p2pkh_2() {
        assertThat(new Base58Address("12higDjoCCNXSA95xZMWUdPvXNmkAduhWv").getScript())
                .isEqualTo(new HexString("76a91412ab8dc588ca9d5787dde7eb29569da63c3a238c88ac"));
    }

    @Test
    void getScript_p2sh() {
        assertThat(new Base58Address(P2SH_2).getScript())
                .isEqualTo(new HexString("a914748284390f9e263a4b766a75d0633c50426eb87587"));
    }

    @Test
    void getScript_not_base58() {
        assertThatIllegalStateException().isThrownBy(
                () -> new Base58Address("4CK4fEwbMP7heJarmU4eqA3sMbVJyEnU3V").getScript()
        ).withMessage("unsupported address type");
    }

    @Test
    void isValid_p2pkh() {
        assertThat(new Base58Address("1EUXSxuUVy2PC5enGXR1a3yxbEjNWMHuem").isValid()).isTrue();
    }

    @Test
    void isValid_p2sh() {
        assertThat(new Base58Address(P2SH).isValid()).isTrue();
    }

    @Test
    void isValid_invalid_character() {
        String addressString = "34nSklnWC9rDDJiUY438qQN1JHmGqBHGW7"; // l instead of i
        assertThat(new Base58Address(addressString).isValid()).isFalse();
    }

    @Test
    void isValid_short_address() {
        assertThat(new Base58Address("11111111111111111111BZbvjr").isValid()).isTrue();
    }

    @Test
    void isValid_too_short() {
        assertThat(new Base58Address("1111111111111111111BZbvjr").isValid()).isFalse();
    }

    @Test
    void isValid_invalid_hash() {
        assertThat(new Base58Address("1EUXSxuUVy2PC5enGXR1a3yxbEjNWMHue7").isValid()).isFalse();
    }

    @Test
    void isValid_bech32() {
        assertThat(new Base58Address(P2WPKH).isValid()).isFalse();
    }
}