package de.cotto.bitbook.backend.model;

import com.google.common.annotations.VisibleForTesting;

import java.math.BigInteger;
import java.util.HexFormat;
import java.util.regex.Pattern;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

public class Base58Address {
    private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final Pattern PATTERN = Pattern.compile("[" + ALPHABET + "]+");

    private static final String P2PKH_PREFIX = "00";
    private static final String P2SH_PREFIX = "05";

    private static final String OP_DUP_HEX = "76";
    private static final String OP_HASH160_HEX = "a9";
    private static final String OP_EQUALVERIFY_HEX = "88";
    private static final String OP_CHECKSIG_HEX = "ac";
    private static final String OP_EQUAL = "87";

    private static final BigInteger FIFTY_EIGHT = BigInteger.valueOf(58);
    private static final int HEX_DIGITS_FOR_CHECKSUM = 8;

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

    public String getScript() {
        String data = getData();
        String hash160 = data.substring(2);
        String numBytes = getLengthInBytesAsHex(hash160);
        if (data.startsWith(P2PKH_PREFIX)) {
            return OP_DUP_HEX + OP_HASH160_HEX + numBytes + hash160 + OP_EQUALVERIFY_HEX + OP_CHECKSIG_HEX;
        } else if (data.startsWith(P2SH_PREFIX)) {
            return OP_HASH160_HEX + numBytes + hash160 + OP_EQUAL;
        } else {
            throw new IllegalStateException("unsupported address type");
        }
    }

    @VisibleForTesting
    String getData() {
        String hex = toHex();
        int checksumCutoffIndex = hex.length() - HEX_DIGITS_FOR_CHECKSUM;
        return hex.substring(0, checksumCutoffIndex);
    }

    @VisibleForTesting
    String toHex() {
        BigInteger total = BigInteger.ZERO;
        String reversed = getReversedAddressString();
        for (int index = 0; index < reversed.length(); index++) {
            char character = reversed.charAt(index);
            BigInteger base58Index = BigInteger.valueOf(ALPHABET.indexOf(character));
            BigInteger value = FIFTY_EIGHT.pow(index).multiply(base58Index);
            total = total.add(value);
        }
        String hex = convertToHexString(total);
        return prependZeros(hex);
    }

    private String prependZeros(String hex) {
        StringBuilder result = new StringBuilder(hex);
        long numberOfLeadingOnes = countLeadingZeros();
        for (int i = 0; i < numberOfLeadingOnes; i++) {
            result.insert(0, P2PKH_PREFIX);
        }
        return result.toString();
    }

    private String getLengthInBytesAsHex(String hexString) {
        return HexFormat.of().formatHex(new byte[]{(byte) (hexString.length() / 2)});
    }

    private String getReversedAddressString() {
        return new StringBuilder(addressString).reverse().toString();
    }

    private long countLeadingZeros() {
        return addressString.chars().takeWhile(c -> c == '1').count();
    }

    private String convertToHexString(BigInteger bigInteger) {
        String hex = bigInteger.toString(16);
        if (hex.length() % 2 != 0) {
            return "0" + hex;
        }
        return hex;
    }

    private boolean checksumIsCorrect() {
        String hex = toHex();
        int checksumCutoffIndex = hex.length() - HEX_DIGITS_FOR_CHECKSUM;
        String expectedChecksum = hex.substring(checksumCutoffIndex);
        String dataToHash = hex.substring(0, checksumCutoffIndex);
        byte[] data = hexStringToByteArray(dataToHash);
        String doubleSha256 = sha256Hex(hexStringToByteArray(sha256Hex(data)));
        return doubleSha256.substring(0, HEX_DIGITS_FOR_CHECKSUM).equals(expectedChecksum);
    }

    private byte[] hexStringToByteArray(String dataToHash) {
        return HexFormat.of().parseHex(dataToHash);
    }
}
