package de.cotto.bitbook.lnd.model;

import java.util.Locale;

public enum Initiator {
    LOCAL, REMOTE, UNKNOWN;

    public static Initiator fromString(String string) {
        if ("INITIATOR_LOCAL".equals(string)) {
            return LOCAL;
        } else if ("INITIATOR_REMOTE".equals(string)) {
            return REMOTE;
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.US);
    }
}
