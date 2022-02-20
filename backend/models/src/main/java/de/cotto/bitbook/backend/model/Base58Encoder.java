package de.cotto.bitbook.backend.model;

public class Base58Encoder {
    private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private final byte[] byteArray;

    public Base58Encoder(HexString input) {
        byteArray = input.getByteArray();
    }

    public String encode() {
        int leadingZeros = getNumberOfLeadingZeros();
        char[] encoded = new char[byteArray.length * 2];
        int outputStart = encoded.length;
        int inputStart = leadingZeros;
        while (inputStart < byteArray.length) {
            outputStart--;
            encoded[outputStart] = ALPHABET.charAt(divmod(byteArray, inputStart));
            if (byteArray[inputStart] == 0) {
                inputStart++;
            }
        }
        while (outputStart < encoded.length && encoded[outputStart] == '1') {
            outputStart++;
        }
        while (leadingZeros > 0) {
            leadingZeros--;
            outputStart--;
            encoded[outputStart] = '1';
        }
        return new String(encoded, outputStart, encoded.length - outputStart);
    }

    private static byte divmod(byte[] number, int firstDigit) {
        int remainder = 0;
        for (int i = firstDigit; i < number.length; i++) {
            int digit = (int) number[i] & 0xFF;
            int temp = remainder * 256 + digit;
            number[i] = (byte) (temp / 58);
            remainder = temp % 58;
        }
        return (byte) remainder;
    }

    private int getNumberOfLeadingZeros() {
        int result = 0;
        for (byte b : byteArray) {
            if (b == 0) {
                result++;
            } else {
                break;
            }
        }
        return result;
    }
}
