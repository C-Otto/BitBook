package de.cotto.bitbook.cli;

import java.util.regex.Pattern;

public class CliTransactionHash extends CliString {
    public static final String ERROR_MESSAGE = "Expected: 64 hex characters";

    private static final Pattern PATTERN = Pattern.compile("[0-9a-fA-F]{64}");
    private static final String INVALID_CHARACTERS_REGEX = "[^0-9a-fA-F]";

    public CliTransactionHash(String transactionHash) {
        super(transactionHash, PATTERN, INVALID_CHARACTERS_REGEX);
    }

    public String getTransactionHash() {
        return toString();
    }
}
