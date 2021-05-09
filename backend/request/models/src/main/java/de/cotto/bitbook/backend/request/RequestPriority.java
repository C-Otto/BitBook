package de.cotto.bitbook.backend.request;

public enum RequestPriority {
    LOWEST(1), STANDARD(0);

    private final int integerForComparison;

    RequestPriority(int integerForComparison) {
        this.integerForComparison = integerForComparison;
    }

    public int getIntegerForComparison() {
        return integerForComparison;
    }

    public boolean isAtLeast(RequestPriority other) {
        return integerForComparison <= other.getIntegerForComparison();
    }

    public RequestPriority getHighestPriority(RequestPriority other) {
        if (isAtLeast(other)) {
            return this;
        }
        return other;
    }
}