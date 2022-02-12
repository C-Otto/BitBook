package de.cotto.bitbook.backend.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static de.cotto.bitbook.backend.model.AddressFixtures.P2TR;
import static de.cotto.bitbook.backend.model.AddressFixtures.P2WPKH;
import static de.cotto.bitbook.backend.model.AddressFixtures.P2WSH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class Bech32AddressTest {
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    class IsValid {
        @Test
        void mixed_lower_upper_case() {
            assertThat(new Bech32Address("bc1q42lja79ELEM0anu8q8s3h2n687re9jax556pcc").isValid()).isFalse();
        }

        @Test
        void upper_case() {
            assertThat(new Bech32Address(P2WPKH.toString().toUpperCase(Locale.US)).isValid()).isTrue();
        }

        @Test
        void p2wpkh() {
            assertThat(new Bech32Address(P2WPKH.toString()).isValid()).isTrue();
        }

        @Test
        void p2wsh() {
            assertThat(new Bech32Address(P2WSH.toString()).isValid()).isTrue();
        }

        @Test
        void long_p2wsh_address() {
            String longAddress = "bc1qgdjqv0av3q56jvd82tkdjpy7gdp9ut8tlqmgrpmv24sq90ecnvqqjwvw97";
            assertThat(new Bech32Address(longAddress).isValid()).isTrue();
        }

        @Test
        void invalid_character() {
            String addressString = "bc1qgdjqv0av3q56jvd82tkdjpy7gdp9ut8tlqmgrpmv24sq90ecnvqqjwvw91"; // 1 is not allowed
            assertThat(new Bech32Address(addressString).isValid()).isFalse();
        }

        @Test
        void invalid_checksum() {
            String addressString = "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t5";
            assertThat(new Bech32Address(addressString).isValid()).isFalse();
        }

        @Test
        void invalid_program_length_1() {
            String addressString = "bc1rw5uspcuh";
            assertThat(new Bech32Address(addressString).isValid()).isFalse();
        }

        @Test
        void invalid_program_length_2() {
            String addressString = "bc10w508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7kw5rljs90";
            assertThat(new Bech32Address(addressString).isValid()).isFalse();
        }

        @Test
        void invalid_program_length_3() {
            // Invalid program length for witness version 0 (per BIP141)
            String addressString = "BC1QR508D6QEJXTDG4Y5R3ZARVARYV98GJ9P";
            assertThat(new Bech32Address(addressString).isValid()).isFalse();
        }

        @Test
        void zero_padding_of_more_than_four_bits() {
            String addressString = "bc1zw508d6qejxtdg4y5r3zarvaryvqyzf3du";
            assertThat(new Bech32Address(addressString).isValid()).isFalse();
        }

        @Test
        void empty_data_section() {
            String addressString = "bc1gmk9yu";
            assertThat(new Bech32Address(addressString).isValid()).isFalse();
        }

        @Test
        void tiny_data_section() {
            String addressString = "bc1gmk9yuu";
            assertThat(new Bech32Address(addressString).isValid()).isFalse();
        }

        @Test
        void invalid_checksum_algorithm_bech32m_instead_of_bech32_a() {
            String addressString = "bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqh2y7hd";
            assertThat(new Bech32Address(addressString).isValid()).isFalse();
        }

        @Test
        void invalid_checksum_algorithm_bech32m_instead_of_bech32_b() {
            String addressString = "BC1S0XLXVLHEMJA6C4DQV22UAPCTQUPFHLXM9H8Z3K2E72Q4K9HCZ7VQ54WELL";
            assertThat(new Bech32Address(addressString).isValid()).isFalse();
        }

        @Test
        void invalid_checksum_algorithm_bech32m_instead_of_bech32_c() {
            String addressString = "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kemeawh";
            assertThat(new Bech32Address(addressString).isValid()).isFalse();
        }

        @Test
        void invalid_character_in_checksum() {
            String addressString = "bc1p38j9r5y49hruaue7wxjce0updqjuyyx0kh56v8s25huc6995vvpql3jow4";
            assertThat(new Bech32Address(addressString).isValid()).isFalse();
        }

        @Test
        void invalid_witness_version_a() {
            String addressString = "BC13W508D6QEJXTDG4Y5R3ZARVARY0C5XW7KN40WF2";
            assertThat(new Bech32Address(addressString).isValid()).isFalse();
        }

        @Test
        void invalid_witness_version_b() {
            String address = "BC130XLXVLHEMJA6C4DQV22UAPCTQUPFHLXM9H8Z3K2E72Q4K9HCZ7VQ7ZWS8R";
            assertThat(new Bech32Address(address).isValid()).isFalse();

        }

        @Test
        void invalid_program_length_1_byte() {
            String address = "bc1pw5dgrnzv";
            assertThat(new Bech32Address(address).isValid()).isFalse();
        }

        @Test
        void invalid_program_length_41_bytes() {
            String address = "bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7v8n0nx0muaewav253zgeav";
            assertThat(new Bech32Address(address).isValid()).isFalse();
        }

        @Test
        void more_than_four_padding_bits() {
            String address = "bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7v07qwwzcrf";
            assertThat(new Bech32Address(address).isValid()).isFalse();
        }

        @Test
        void p2tr() {
            String address = P2TR.toString();
            assertThat(new Bech32Address(address).isValid()).isTrue();
        }

        @Test
        void version16() {
            assertThat(new Bech32Address("BC1SW50QA3JX3S").isValid()).isFalse();
        }
    }

    @Test
    void getScript_bip_173_example() {
        String address = "BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4";
        assertThat(new Bech32Address(address).getScript())
                .isEqualTo("0014751e76e8199196d454941c45d1b3a323f1433bd6");
    }

    @Test
    void getScript_p2wpkh() {
        String address = P2WPKH.toString();
        assertThat(new Bech32Address(address).getScript())
                .isEqualTo("0014aabf2ef8b9fe76fecf8701e11baa7a3f8792cba6");
    }

    @Test
    void getScript_p2wsh() {
        String address = P2WSH.toString();
        assertThat(new Bech32Address(address).getScript())
                .isEqualTo("00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262");
    }

    @Test
    void getScript_unsupported() {
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
                () -> new Bech32Address("BC1SW50QGDZ25J").getScript()
        );
    }
}