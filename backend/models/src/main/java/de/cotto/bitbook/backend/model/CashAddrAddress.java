package de.cotto.bitbook.backend.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class CashAddrAddress extends Bech32Base {
    private static final String BITCOIN_CASH_PREFIX = "bitcoincash:";
    private static final Pattern PATTERN = Pattern.compile("bitcoincash:[" + CHARSET + "]{6,}");
    private static final int P2PKH_VERSION = 0;
    private static final int P2SH_VERSION = 8;
    private static final int RIPE160_HASH_LENGTH = 20;

    private final String addressString;
    private final String lowerCaseWithPrefix;

    CashAddrAddress(String addressString) {
        super();
        this.addressString = addressString;
        lowerCaseWithPrefix = prependPrefixIfMissing(addressString.toLowerCase(Locale.US));
        payloadWithChecksum = getPayloadWithChecksum();
    }

    public boolean isValid() {
        if (hasInvalidFormat()) {
            return false;
        }
        return hasCorrectChecksum();
    }

    public String getLegacyAddress() {
        if (!isValid()) {
            return addressString;
        }
        Objects.requireNonNull(payloadWithChecksum);
        byte[] data = Arrays.copyOfRange(payloadWithChecksum, 12, payloadWithChecksum.length - 8);
        HexString converted = convertBits(data);
        byte version = (byte) (converted.getByteArray()[0] & 120);
        HexString hash = converted.getSubstringStartingAtByte(1);
        if (version == P2PKH_VERSION && hash.getNumberOfBytes() == RIPE160_HASH_LENGTH) {
            return Base58Address.createP2Pkh(hash);
        } else if (version == P2SH_VERSION && hash.getNumberOfBytes() == RIPE160_HASH_LENGTH) {
            return Base58Address.createP2Sh(hash);
        } else {
            return addressString;
        }
    }

    private String prependPrefixIfMissing(String addressString) {
        if (addressString.toLowerCase(Locale.US).startsWith(BITCOIN_CASH_PREFIX)) {
            return addressString;
        }
        return BITCOIN_CASH_PREFIX + addressString;
    }

    private static HexString convertBits(byte[] bytes) {
        int mask = 255;
        List<Byte> result = new ArrayList<>();
        int accumulator = 0;
        int bits = 0;
        for (byte value : bytes) {
            accumulator = ((accumulator & 0xff) * 32) | (value & 0xff);
            bits += 5;
            while (bits >= 8) {
                bits -= 8;
                result.add((byte) ((accumulator >> bits) & mask));
            }
        }
        return new HexString(result);
    }

    private boolean hasInvalidFormat() {
        String addressWithPrefix = prependPrefixIfMissing(addressString);
        String addressWithoutPrefix = addressWithPrefix.substring(BITCOIN_CASH_PREFIX.length());
        String upperCaseWithPrefix = prependPrefixIfMissing(addressString.toUpperCase(Locale.US));
        String lowerCaseWithoutPrefix = lowerCaseWithPrefix.substring(BITCOIN_CASH_PREFIX.length());
        String upperCaseWithoutPrefix = upperCaseWithPrefix.substring(BITCOIN_CASH_PREFIX.length());
        boolean isLowerCase = lowerCaseWithoutPrefix.equals(addressWithoutPrefix);
        boolean isUpperCase = upperCaseWithoutPrefix.equals(addressWithoutPrefix);
        boolean mixedCase = !(isLowerCase || isUpperCase);
        boolean invalidPattern = !PATTERN.matcher(lowerCaseWithPrefix).matches();
        return mixedCase || invalidPattern;
    }

    private boolean hasCorrectChecksum() {
        return polymod(Objects.requireNonNull(payloadWithChecksum)) == 0;
    }

    @SuppressWarnings("PMD.CognitiveComplexity")
    private long polymod(byte[] values) {
        long checksum = 1;
        for (long value : values) {
            long top = checksum >> 35;
            checksum = (checksum & 0x07ffffffffL) << BITS_FOR_BECH32_CHAR ^ value;
            for (int i = 0; i < BITS_FOR_BECH32_CHAR; i++) {
                if ((top & 0x01) != 0) {
                    checksum ^= 0x98f2bc8e61L;
                }
                if ((top & 0x02) != 0) {
                    checksum ^= 0x79b76d99e2L;
                }
                if ((top & 0x04) != 0) {
                    checksum ^= 0xf33e5fb3c4L;
                }
                if ((top & 0x08) != 0) {
                    checksum ^= 0xae2eabe2a8L;
                }
                if ((top & 0x10) != 0) {
                    checksum ^= 0x1e4f43e470L;
                }
            }
        }
        return checksum ^ 1;
    }

    private byte[] getPayloadWithChecksum() {
        byte[] prefix = {2, 9, 20, 3, 15, 9, 14, 3, 1, 19, 8, 0};
        int offset = BITCOIN_CASH_PREFIX.length();
        byte[] payload = new byte[lowerCaseWithPrefix.length()];
        System.arraycopy(prefix, 0, payload, 0, prefix.length);
        for (int position = offset; position < lowerCaseWithPrefix.length(); position++) {
            payload[position] = (byte) CHARSET.indexOf(lowerCaseWithPrefix.charAt(position));
        }
        return payload;
    }
}
