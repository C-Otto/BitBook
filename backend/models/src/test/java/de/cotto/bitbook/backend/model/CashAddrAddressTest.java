package de.cotto.bitbook.backend.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

class CashAddrAddressTest {

    private static final String CASH_ADDR = "qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a";
    private static final String CASH_ADDR_WITH_PREFIX = "bitcoincash:" + CASH_ADDR;

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    class IsValid {
        @Test
        void mixed_lower_upper_case() {
            assertThat(new CashAddrAddress("qPm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a").isValid()).isFalse();
        }

        @Test
        void mixed_lower_upper_case_ignores_prefix() {
            assertThat(new CashAddrAddress("BiTCoInCash:" + CASH_ADDR).isValid()).isTrue();
        }

        @Test
        void upper_case_with_prefix() {
            assertThat(new CashAddrAddress(CASH_ADDR_WITH_PREFIX.toUpperCase(Locale.US)).isValid()).isTrue();
        }

        @Test
        void upper_case() {
            assertThat(new CashAddrAddress(CASH_ADDR.toUpperCase(Locale.US)).isValid()).isTrue();
        }

        @Test
        void cashaddr() {
            assertThat(new CashAddrAddress(CASH_ADDR).isValid()).isTrue();
        }

        @Test
        void cashaddr_with_prefix() {
            assertThat(new CashAddrAddress(CASH_ADDR_WITH_PREFIX).isValid()).isTrue();
        }

        @Test
        void invalid_character() {
            String addressString = "qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx61"; // 1 is not allowed
            assertThat(new CashAddrAddress(addressString).isValid()).isFalse();
        }

        @Test
        void invalid_checksum() {
            String addressString = "qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx7a";
            assertThat(new CashAddrAddress(addressString).isValid()).isFalse();
        }
    }

    @Test
    void getLegacyAddress_empty_is_empty() {
        assertThat(new CashAddrAddress("").getLegacyAddress()).isEqualTo("");
    }

    @Test
    void getLegacyAddress_returns_original_input_if_not_valid() {
        String invalidAddress = "hello how are you?";
        assertThat(new CashAddrAddress(invalidAddress).getLegacyAddress()).isEqualTo(invalidAddress);
    }

    @Test
    void getLegacyAddress_unsupported_hash_length_type_0() {
        String hash24BytesType0 = "bitcoincash:q9adhakpwzztepkpwp5z0dq62m6u5v5xtyj7j3h2ws4mr9g0";
        CashAddrAddress cashAddrAddress = new CashAddrAddress(hash24BytesType0);
        assumeThat(cashAddrAddress.isValid()).isTrue();
        assertThat(cashAddrAddress.getLegacyAddress()).isEqualTo(hash24BytesType0);
    }

    @Test
    void getLegacyAddress_p2pkh() {
        assertThat(new CashAddrAddress("qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a").getLegacyAddress())
                .isEqualTo("1BpEi6DfDAUFd7GtittLSdBeYJvcoaVggu");
    }

    @Test
    void getLegacyAddress_p2sh() {
        assertThat(new CashAddrAddress("pr95sy3j9xwd2ap32xkykttr4cvcu7as4yc93ky28e").getLegacyAddress())
                .isEqualTo("3LDsS579y7sruadqu11beEJoTjdFiFCdX4");
    }
}