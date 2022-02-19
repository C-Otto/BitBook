package de.cotto.bitbook.backend.model;

import javax.annotation.CheckForNull;
import java.util.Objects;

public class Bech32Base {
    protected static final int BITS_FOR_BECH32_CHAR = 5;
    protected static final String CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";
    private static final int CHECKSUM_BYTES = 6;
    @CheckForNull
    protected byte[] payloadWithChecksum;

    Bech32Base() {
        // default constructor
    }

    protected byte[] getPayload() {
        byte[] payload = new byte[Objects.requireNonNull(payloadWithChecksum).length - CHECKSUM_BYTES];
        System.arraycopy(payloadWithChecksum, 0, payload, 0, payload.length);
        return payload;
    }
}
