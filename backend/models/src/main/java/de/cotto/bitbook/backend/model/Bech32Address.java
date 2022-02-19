package de.cotto.bitbook.backend.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class Bech32Address extends Bech32Base {
    private static final int WITNESS_VERSION_0 = 0;
    private static final int WITNESS_VERSION_1 = 1;
    private static final int BECH32M_CONST = 0x2bc830a3;
    private static final Pattern PATTERN = Pattern.compile("bc1[" + CHARSET + "]{1,87}");
    private static final int HUMAN_READABLE_PART_END_INDEX = 2;
    private static final int[] GENERATOR = {0x3b6a57b2, 0x26508e6d, 0x1ea119fa, 0x3d4233dd, 0x2a1462b3};
    private static final byte DATA_LENGTH_P2PKH_WIT = 20;
    private static final byte DATA_LENGTH_P2SH_WIT = 32;
    private static final byte DATA_LENGTH_P2TR_WIT = 32;
    private static final HexString OP_0_HEX = new HexString((byte) 0);
    private static final HexString OP_1_HEX = new HexString((byte) 81);

    private final String addressString;
    private final String lowerCase;

    Bech32Address(String addressString) {
        super();
        this.addressString = addressString;
        lowerCase = addressString.toLowerCase(Locale.US);
        payloadWithChecksum = getPayloadWithChecksum();
    }

    public boolean isValid() {
        if (hasInvalidFormat()) {
            return false;
        }
        if (hasIncorrectChecksum()) {
            return false;
        }
        return decodedDataIsValid();
    }

    public HexString getScript() {
        HexString decoded = getDecoded();
        byte witnessVersion = getWitnessVersion();
        if (witnessVersion == WITNESS_VERSION_0) {
            if (decoded.getNumberOfBytes() == DATA_LENGTH_P2PKH_WIT) {
                HexString dataLength = new HexString(DATA_LENGTH_P2PKH_WIT);
                return OP_0_HEX
                        .append(dataLength)
                        .append(decoded);
            } else if (decoded.getNumberOfBytes() == DATA_LENGTH_P2SH_WIT) {
                HexString dataLength = new HexString(DATA_LENGTH_P2SH_WIT);
                return OP_0_HEX
                        .append(dataLength)
                        .append(decoded);
            }
        }
        if (witnessVersion == WITNESS_VERSION_1 && decoded.getNumberOfBytes() == DATA_LENGTH_P2TR_WIT) {
            HexString dataLength = new HexString(DATA_LENGTH_P2TR_WIT);
            return OP_1_HEX
                    .append(dataLength)
                    .append(decoded);
        }
        throw new IllegalStateException("unsupported address type");
    }

    private boolean hasInvalidFormat() {
        String upperCase = addressString.toUpperCase(Locale.US);
        boolean mixedCase = !lowerCase.equals(addressString) && !upperCase.equals(addressString);
        boolean invalidPattern = !PATTERN.matcher(lowerCase).matches();
        return mixedCase || invalidPattern;
    }

    private boolean hasIncorrectChecksum() {
        return !hasCorrectChecksum();
    }

    private boolean hasCorrectChecksum() {
        if (getPayload().length == 0) {
            return false;
        }
        String humanReadablePart = lowerCase.substring(0, HUMAN_READABLE_PART_END_INDEX);
        byte[] expandedHumanReadablePart = getExpandedHumanReadablePart(humanReadablePart);
        Objects.requireNonNull(payloadWithChecksum);
        byte[] dataToHash = new byte[expandedHumanReadablePart.length + payloadWithChecksum.length];
        System.arraycopy(expandedHumanReadablePart, 0, dataToHash, 0, expandedHumanReadablePart.length);
        System.arraycopy(
                payloadWithChecksum, 0, dataToHash,
                expandedHumanReadablePart.length, payloadWithChecksum.length
        );
        int checksum = polymod(dataToHash);
        byte witnessVersion = getWitnessVersion();
        boolean bech32 = witnessVersion == WITNESS_VERSION_0 && checksum == 1;
        boolean bech32m = witnessVersion != WITNESS_VERSION_0 && checksum == BECH32M_CONST;
        return bech32 || bech32m;
    }

    private boolean decodedDataIsValid() {
        HexString decoded = getDecoded();
        int numberOfBytes = decoded.getNumberOfBytes();
        byte witnessVersion = getWitnessVersion();
        //noinspection EnhancedSwitchMigration
        switch (witnessVersion) {
            case 0:
                return numberOfBytes == DATA_LENGTH_P2PKH_WIT || numberOfBytes == DATA_LENGTH_P2SH_WIT;
            case 1:
                return numberOfBytes == DATA_LENGTH_P2TR_WIT;
            default:
                return false;
        }
    }

    private byte getWitnessVersion() {
        byte[] payload = getPayload();
        return payload[0];
    }

    private byte[] getExpandedHumanReadablePart(String humanReadablePart) {
        byte[] expandedHumanReadablePart = new byte[2 * humanReadablePart.length() + 1];
        expandedHumanReadablePart[humanReadablePart.length()] = 0;
        for (int i = 0; i < humanReadablePart.length(); i++) {
            char value = humanReadablePart.charAt(i);
            expandedHumanReadablePart[i] = (byte) (value >> BITS_FOR_BECH32_CHAR);
            expandedHumanReadablePart[humanReadablePart.length() + 1 + i] = (byte) (value & 31);
        }
        return expandedHumanReadablePart;
    }

    private int polymod(byte[] values) {
        int checksum = 1;
        for (int value : values) {
            int top = checksum >> 25;
            checksum = (checksum & 0x1ffffff) << BITS_FOR_BECH32_CHAR ^ value;
            for (int i = 0; i < BITS_FOR_BECH32_CHAR; i++) {
                if (((top >> i) & 1) != 0) {
                    checksum ^= GENERATOR[i];
                }
            }
        }
        return checksum;
    }

    private HexString getDecoded() {
        boolean isFirst = true;
        int acc = 0;
        int bits = 0;
        List<Byte> resultList = new ArrayList<>();
        int maxV = 255;
        int maxAcc = 4095;
        for (int value : getPayload()) {
            if (isFirst) {
                isFirst = false;
                continue;
            }
            if (value < 0 || value >> BITS_FOR_BECH32_CHAR != 0) {
                return HexString.EMPTY;
            }
            acc = ((acc << BITS_FOR_BECH32_CHAR) | value) & maxAcc;
            bits += BITS_FOR_BECH32_CHAR;
            while (bits >= 8) {
                bits -= 8;
                resultList.add((byte) ((acc >> bits) & maxV));
            }
        }
        int bitDifference = 8 - bits;
        if (bits >= BITS_FOR_BECH32_CHAR || (acc << bitDifference & maxV) != 0) {
            return HexString.EMPTY;
        }
        return new HexString(resultList);
    }

    private byte[] getPayloadWithChecksum() {
        int offset = HUMAN_READABLE_PART_END_INDEX + 1;
        byte[] payload = new byte[lowerCase.length() - offset];
        for (int position = offset; position < lowerCase.length(); position++) {
            payload[position - offset] = (byte) CHARSET.indexOf(lowerCase.charAt(position));
        }
        return payload;
    }
}
