package de.cotto.bitbook.cli;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CliAddressTest {

    private static final String VALID_ADDRESS = "1QLbz7JHiBTspS962RLKV8GndWFwi5j6Qr";
    private static final String SHORT_ADDRESS = "bc1qngw83";

    @Test
    void errorMessage() {
        assertThat(CliAddress.ERROR_MESSAGE).isEqualTo("Expected base58 or bech32 address");
    }

    @Test
    void getAddress() {
        assertThat(new CliAddress(VALID_ADDRESS).getAddress()).isEqualTo(VALID_ADDRESS);
    }

    @Test
    void getAddress_sanitized() {
        assertThat(new CliAddress("\t1QLbz7JHiBTspS962RLKV8GndWFwi5j6Qr,:! ").getAddress())
                .isEqualTo("1QLbz7JHiBTspS962RLKV8GndWFwi5j6Qr");
    }

    @Test
    void getAddress_invalid() {
        assertThat(new CliAddress("1QLbz").getAddress()).isEqualTo("");
    }

    @Test
    void getAddress_base58_p2pkh() {
        assertThat(new CliAddress(VALID_ADDRESS).getAddress()).isEqualTo(VALID_ADDRESS);
    }

    @Test
    void getAddress_base58_p2sh() {
        assertThat(new CliAddress("3DnW8JGpPViEZdpqat8qky1zc26EKbXnmM").getAddress())
                .isEqualTo("3DnW8JGpPViEZdpqat8qky1zc26EKbXnmM");
    }

    @Test
    void getAddress_base58_short() {
        assertThat(new CliAddress("11111111111111111111BZbvjr").getAddress())
                .isEqualTo("11111111111111111111BZbvjr");
    }

    @Test
    void getAddress_bech32() {
        assertThat(new CliAddress("bc1qngw83fg8dz0k749cg7k3emc7v98wy0c74dlrkd").getAddress())
                .isEqualTo("bc1qngw83fg8dz0k749cg7k3emc7v98wy0c74dlrkd");
    }

    @Test
    void getAddress_bech32_short() {
        assertThat(new CliAddress(SHORT_ADDRESS).getAddress()).isEqualTo(SHORT_ADDRESS);
    }

    @Test
    void getAddress_bech32_long() {
        String longBech32Address =
                "bc1qngw83fg8dz0k749cg7k3emc7v98wy0c74dlrkdaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        assertThat(new CliAddress(longBech32Address).getAddress())
                .isEqualTo(longBech32Address);
    }

    @Test
    void splits_on_no_breaking_space() {
        assertThat(new CliAddress("bc1qngw83\u00a0xxx").getAddress()).isEqualTo(SHORT_ADDRESS);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(CliAddress.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        assertThat(new CliAddress(SHORT_ADDRESS)).hasToString(SHORT_ADDRESS);
    }

    @Test
    void testToString_garbage() {
        assertThat(new CliAddress("    !bc1qngw83:\u00a0xxx")).hasToString(SHORT_ADDRESS);
    }
}