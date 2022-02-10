package de.cotto.bitbook.backend.model;

import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.AddressFixtures.P2TR;
import static de.cotto.bitbook.backend.model.AddressFixtures.P2WPKH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class Base58AddressTest {

    private static final String P2SH = AddressFixtures.P2SH.toString();
    private static final String P2SH_2 = AddressFixtures.P2SH_2.toString();
    private static final String P2PKH = AddressFixtures.P2PKH.toString();

    @Test
    void getScript_p2pkh() {
        assertThat(new Base58Address(P2PKH).getScript())
                .isEqualTo("76a91462e907b15cbf27d5425399ebf6f0fb50ebb88f1888ac");
    }

    @Test
    void getScript_p2pkh_2() {
        assertThat(new Base58Address("12higDjoCCNXSA95xZMWUdPvXNmkAduhWv").getScript())
                .isEqualTo("76a91412ab8dc588ca9d5787dde7eb29569da63c3a238c88ac");
    }

    @Test
    void getScript_p2sh() {
        assertThat(new Base58Address(P2SH_2).getScript())
                .isEqualTo("a914748284390f9e263a4b766a75d0633c50426eb87587");
    }

    @Test
    void getScript_not_base58() {
        assertThatIllegalStateException().isThrownBy(
                () -> new Base58Address(P2TR.getScript()).getScript()
        ).withMessage("unsupported address type");
    }

    @Test
    void p2pkh_getData_all_zeros() {
        assertThat(new Base58Address("1111111111111111111114oLvT2").getData())
                .isEqualTo("000000000000000000000000000000000000000000");
    }

    @Test
    void p2pkh_getData() {
        assertThat(new Base58Address("1AKDDsfTh8uY4X3ppy1m7jw1fVMBSMkzjP").getData())
                .isEqualTo("00662ad25db00e7bb38bc04831ae48b4b446d12698");
    }

    @Test
    void p2sh_getData() {
        assertThat(new Base58Address(P2SH).getData())
                .isEqualTo("0521ef2f4b1ea1f9ed09c1128d1ebb61d4729ca7d6");
    }

    @Test
    void p2pkh_toHex_simple() {
        assertThat(new Base58Address("1A").toHex())
                .isEqualTo("0009");
    }

    @Test
    void p2pkh_toHex_leading_ones() {
        assertThat(new Base58Address("11111A").toHex())
                .isEqualTo("000000000009");
    }

    @Test
    void p2pkh_toHex_longer() {
        assertThat(new Base58Address("1AB").toHex())
                .isEqualTo("000214");
    }

    @Test
    void p2pkh_toHex_larger_than_long() {
        assertThat(new Base58Address("1EUXSxuUVy2PC").toHex())
                .isEqualTo("0012406eb4c8296c200b");
    }

    @Test
    void p2pkh_toHex() {
        assertThat(new Base58Address("1EUXSxuUVy2PC5enGXR1a3yxbEjNWMHuem").toHex())
                .isEqualTo("0093ce48570b55c42c2af816aeaba06cfee1224faebb6127fe");
    }

    @Test
    void p2sh_toHex() {
        assertThat(new Base58Address(P2SH).toHex())
                .isEqualTo("0521ef2f4b1ea1f9ed09c1128d1ebb61d4729ca7d6acd16c94");
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
    void isValid_invalid_hash() {
        assertThat(new Base58Address("1EUXSxuUVy2PC5enGXR1a3yxbEjNWMHue7").isValid()).isFalse();
    }

    @Test
    void isValid_bech32() {
        assertThat(new Base58Address(P2WPKH.toString()).isValid()).isFalse();
    }
}