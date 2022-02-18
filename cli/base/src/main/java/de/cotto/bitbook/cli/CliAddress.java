package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.model.Address;

import java.util.regex.Pattern;

public class CliAddress extends CliString {
    public static final String ERROR_MESSAGE = "Expected base58 or bech32 address";

    private static final String INVALID_CHARACTERS_REGEX = "[^0-9a-zA-Z]";

    private static final String BECH_32 = "bc\\d[ac-hj-np-zA-HJ-NP-Z02-9]{6,87}";
    private static final String BASE_58 = "[1-9A-HJ-NP-Za-km-z]{20,35}";
    private static final String CASH_ADDR = "[qpzry9x8gf2tvdw0s3jn54khce6mua7l]{6,}";
    private static final Pattern PATTERN = Pattern.compile(BECH_32 + "|" + BASE_58 + "|" + CASH_ADDR);
    private static final String BITCOIN_CASH_PREFIX = "bitcoincash:";

    public CliAddress(Address address) {
        this(address.toString());
    }

    public CliAddress(String address) {
        super(getWithoutBitcoinCashPrefix(address), PATTERN, INVALID_CHARACTERS_REGEX);
    }

    private static String getWithoutBitcoinCashPrefix(String address) {
        if (address.startsWith(BITCOIN_CASH_PREFIX)) {
            return address.substring(BITCOIN_CASH_PREFIX.length());
        }
        return address;
    }

    public Address getAddress() {
        return new Address(toString());
    }
}
