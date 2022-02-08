package de.cotto.bitbook.backend.model;

public record Address(String address) implements Comparable<Address> {
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

    @Override
    public int compareTo(Address other) {
        return address.compareTo(other.address);
    }
}
