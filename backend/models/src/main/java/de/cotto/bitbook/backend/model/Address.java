package de.cotto.bitbook.backend.model;

public record Address(String address) {
    public static final Address NONE = new Address("");

    public boolean isValid() {
        return !isInvalid();
    }

    public boolean isInvalid() {
        return address.isEmpty();
    }

    @Override
    public String toString() {
        return address;
    }
}
