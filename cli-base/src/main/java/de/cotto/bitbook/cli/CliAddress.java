package de.cotto.bitbook.cli;

import java.util.Objects;
import java.util.regex.Pattern;

public class CliAddress {
    public static final String ERROR_MESSAGE = "Expected base58 or bech32 address";

    private final String address;
    private static final Pattern BECH_32 = Pattern.compile("bc\\d[ac-hj-np-zA-HJ-NP-Z02-9]{6,87}");
    private static final Pattern BASE_58 = Pattern.compile("[1-9A-HJ-NP-Za-km-z]{20,35}");

    public CliAddress(String address) {
        if (address.contains("\u00a0")) {
            this.address = sanitize(address.substring(0, address.indexOf('\u00a0')));
        } else {
            this.address = sanitize(address);
        }
    }

    private String sanitize(String address) {
        String sanitized = address.replaceAll("[^0-9a-zA-Z]", "");
        if (BECH_32.matcher(sanitized).matches()) {
            return sanitized;
        }
        if (BASE_58.matcher(sanitized).matches()) {
            return sanitized;
        }
        return "";
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        CliAddress that = (CliAddress) other;
        return Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }

    @Override
    public String toString() {
        return address;
    }
}
