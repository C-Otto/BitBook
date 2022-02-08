package de.cotto.bitbook.backend.model;

public record TransactionHash(String hash) implements Comparable<TransactionHash> {
    public static final TransactionHash NONE = new TransactionHash("");

    public boolean isInvalid() {
        return hash.isBlank();
    }

    public boolean isValid() {
        return !isInvalid();
    }

    @Override
    public int compareTo(TransactionHash other) {
        return hash.compareTo(other.hash);
    }

    @Override
    public String toString() {
        return hash;
    }
}
