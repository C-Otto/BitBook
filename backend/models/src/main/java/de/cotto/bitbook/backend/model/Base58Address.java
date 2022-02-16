package de.cotto.bitbook.backend.model;

import java.math.BigInteger;
import java.util.regex.Pattern;

public class Base58Address {
    private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final Pattern PATTERN = Pattern.compile("[" + ALPHABET + "]{26,}");

    private static final HexString P2PKH_PREFIX = new HexString("00");
    private static final HexString P2SH_PREFIX = new HexString("05");

    private static final HexString OP_DUP_HEX = new HexString("76");
    private static final HexString OP_HASH160_HEX = new HexString("a9");
    private static final HexString OP_EQUALVERIFY_HEX = new HexString("88");
    private static final HexString OP_CHECKSIG_HEX = new HexString("ac");
    private static final HexString OP_EQUAL_HEX = new HexString("87");

    private static final BigInteger FIFTY_EIGHT = BigInteger.valueOf(58);
    private static final int BYTES_FOR_CHECKSUM = 4;

    private final String addressString;

    Base58Address(String addressString) {
        this.addressString = addressString;
    }

    public boolean isValid() {
        boolean matches = PATTERN.matcher(addressString).matches();
        if (matches) {
            return checksumIsCorrect();
        }
        return false;
    }

    public HexString getScript() {
        HexString data = getData();
        HexString hash160 = data.getSubstringStartingAtByte(1);
        HexString numBytes = getLengthInBytesAsHex(hash160);
        HexString firstByte = data.getSubstringUpToByte(1);
        if (firstByte.equals(P2PKH_PREFIX)) {
            return OP_DUP_HEX
                    .append(OP_HASH160_HEX)
                    .append(numBytes)
                    .append(hash160)
                    .append(OP_EQUALVERIFY_HEX)
                    .append(OP_CHECKSIG_HEX);
        } else if (firstByte.equals(P2SH_PREFIX)) {
            return OP_HASH160_HEX
                    .append(numBytes)
                    .append(hash160)
                    .append(OP_EQUAL_HEX);
        } else {
            throw new IllegalStateException("unsupported address type");
        }
    }

    private HexString getData() {
        HexString hex = toHex();
        int numberOfDataBytes = hex.getNumberOfBytes() - BYTES_FOR_CHECKSUM;
        return hex.getSubstringUpToByte(numberOfDataBytes);
    }

    private HexString toHex() {
        BigInteger total = BigInteger.ZERO;
        String reversed = getReversedAddressString();
        for (int index = 0; index < reversed.length(); index++) {
            char character = reversed.charAt(index);
            BigInteger base58Index = BigInteger.valueOf(ALPHABET.indexOf(character));
            BigInteger value = FIFTY_EIGHT.pow(index).multiply(base58Index);
            total = total.add(value);
        }
        HexString hex = convertToHexString(total);
        return prependZeros(hex);
    }

    private HexString prependZeros(HexString hex) {
        HexString result = hex;
        long numberOfLeadingOnes = countLeadingZeros();
        for (int i = 0; i < numberOfLeadingOnes; i++) {
            result = P2PKH_PREFIX.append(result);
        }
        return result;
    }

    private HexString getLengthInBytesAsHex(HexString hexString) {
        byte numberOfBytes = (byte) hexString.getNumberOfBytes();
        return new HexString(numberOfBytes);
    }

    private String getReversedAddressString() {
        return new StringBuilder(addressString).reverse().toString();
    }

    private long countLeadingZeros() {
        return addressString.chars().takeWhile(c -> c == '1').count();
    }

    private HexString convertToHexString(BigInteger bigInteger) {
        String hex = bigInteger.toString(16);
        if (hex.length() % 2 != 0) {
            return new HexString("0" + hex);
        }
        return new HexString(hex);
    }

    private boolean checksumIsCorrect() {
        HexString hex = toHex();
        int numberOfDataBytes = hex.getNumberOfBytes() - BYTES_FOR_CHECKSUM;
        HexString expectedChecksum = hex.getSubstringStartingAtByte(numberOfDataBytes);
        HexString dataToHash = hex.getSubstringUpToByte(numberOfDataBytes);
        HexString doubleSha256 = dataToHash.getSha256Hash().getSha256Hash();
        return doubleSha256.getSubstringUpToByte(BYTES_FOR_CHECKSUM).equals(expectedChecksum);
    }
}
