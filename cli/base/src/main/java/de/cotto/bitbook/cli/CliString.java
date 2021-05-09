package de.cotto.bitbook.cli;

import java.util.Objects;
import java.util.regex.Pattern;

public class CliString {
    private final String string;
    private final Pattern pattern;
    private final String invalidCharactersRegex;

    public CliString(String string, Pattern pattern, String invalidCharactersRegex) {
        this.pattern = pattern;
        this.invalidCharactersRegex = invalidCharactersRegex;
        if (string.contains("\u00a0")) {
            this.string = sanitize(string.substring(0, string.indexOf('\u00a0')));
        } else {
            this.string = sanitize(string);
        }
    }

    private String sanitize(String transactionHash) {
        String sanitized = transactionHash.replaceAll(invalidCharactersRegex, "");
        if (pattern.matcher(sanitized).matches()) {
            return sanitized;
        }
        return "";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        CliString cliString = (CliString) other;
        return Objects.equals(string, cliString.string)
               && Objects.equals(pattern, cliString.pattern)
               && Objects.equals(invalidCharactersRegex, cliString.invalidCharactersRegex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(string, pattern, invalidCharactersRegex);
    }

    @Override
    public String toString() {
        return string;
    }
}
