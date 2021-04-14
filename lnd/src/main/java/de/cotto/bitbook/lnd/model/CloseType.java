package de.cotto.bitbook.lnd.model;

public enum CloseType {
    COOPERATIVE("cooperative"),
    COOPERATIVE_REMOTE("cooperative remote"),
    COOPERATIVE_LOCAL("cooperative local"),
    FORCE_REMOTE("force remote"),
    FORCE_LOCAL("force local");

    private final String stringRepresentation;

    CloseType(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    public static CloseType fromStringAndInitiator(String closeType, String closeInitiatorString) {
        if ("REMOTE_FORCE_CLOSE".equals(closeType)) {
            return FORCE_REMOTE;
        }
        if ("LOCAL_FORCE_CLOSE".equals(closeType)) {
            return FORCE_LOCAL;
        }
        Initiator closeInitiator = Initiator.fromString(closeInitiatorString);
        if (closeInitiator.equals(Initiator.REMOTE)) {
            return COOPERATIVE_REMOTE;
        }
        if (closeInitiator.equals(Initiator.LOCAL)) {
            return COOPERATIVE_LOCAL;
        }
        return COOPERATIVE;
    }

    @Override
    public String toString() {
        return stringRepresentation;
    }
}
